package org.lanjianghao.daijia.map.service.impl;

import com.alibaba.fastjson.JSON;
import org.bson.types.ObjectId;
import org.lanjianghao.daijia.common.constant.RedisConstant;
import org.lanjianghao.daijia.common.constant.SystemConstant;
import org.lanjianghao.daijia.common.util.LocationUtil;
import org.lanjianghao.daijia.driver.client.DriverInfoFeignClient;
import org.lanjianghao.daijia.map.repository.OrderServiceLocationRepository;
import org.lanjianghao.daijia.map.service.LocationService;
import lombok.extern.slf4j.Slf4j;
import org.lanjianghao.daijia.model.entity.driver.DriverSet;
import org.lanjianghao.daijia.model.entity.map.OrderServiceLocation;
import org.lanjianghao.daijia.model.form.map.OrderServiceLocationForm;
import org.lanjianghao.daijia.model.form.map.SearchNearByDriverForm;
import org.lanjianghao.daijia.model.form.map.UpdateDriverLocationForm;
import org.lanjianghao.daijia.model.form.map.UpdateOrderLocationForm;
import org.lanjianghao.daijia.model.vo.map.NearByDriverVo;
import org.lanjianghao.daijia.model.vo.map.OrderLocationVo;
import org.lanjianghao.daijia.model.vo.map.OrderServiceLastLocationVo;
import org.lanjianghao.daijia.order.client.OrderInfoFeignClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.data.geo.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class LocationServiceImpl implements LocationService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private DriverInfoFeignClient driverInfoFeignClient;

    @Autowired
    private OrderServiceLocationRepository orderServiceLocationRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private OrderInfoFeignClient orderInfoFeignClient;

    @Override
    public boolean updateDriverLocation(UpdateDriverLocationForm updateDriverLocationForm) {
        double longitude = updateDriverLocationForm.getLongitude().doubleValue();
        double latitude = updateDriverLocationForm.getLatitude().doubleValue();
        Long driverId = updateDriverLocationForm.getDriverId();
        redisTemplate.opsForGeo().add(RedisConstant.DRIVER_GEO_LOCATION,
                new Point(longitude, latitude), driverId.toString());
        return true;
    }

    @Override
    public boolean removeDriverLocation(Long driverId) {
        redisTemplate.opsForGeo().remove(RedisConstant.DRIVER_GEO_LOCATION, driverId.toString());
        return true;
    }

    @Override
    public List<NearByDriverVo> searchNearByDriver(SearchNearByDriverForm form) {
        double longitude = form.getLongitude().doubleValue();
        double latitude = form.getLatitude().doubleValue();
        RedisGeoCommands.GeoRadiusCommandArgs args =
                RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                        .includeDistance()
                        .includeCoordinates()
                        .sortAscending();
        GeoResults<RedisGeoCommands.GeoLocation<String>> geoRes = redisTemplate.opsForGeo().search(
                RedisConstant.DRIVER_GEO_LOCATION,
                GeoReference.fromCoordinate(longitude, latitude),
                new Distance(SystemConstant.NEARBY_DRIVER_RADIUS, Metrics.KILOMETERS),
                args);

        if (geoRes == null || CollectionUtils.isEmpty(geoRes.getContent())) {
            return Collections.emptyList();
        }
        List<NearByDriverVo> res = geoRes.getContent().stream().map(item -> {
            NearByDriverVo vo = new NearByDriverVo();
            vo.setDriverId(Long.parseLong(item.getContent().getName()));
            BigDecimal distance = BigDecimal.valueOf(item.getDistance().getNormalizedValue())
                    .multiply(BigDecimal.valueOf(Metrics.KILOMETERS.getMultiplier()))
                    .setScale(2, RoundingMode.HALF_UP);
            vo.setDistance(distance);
            return vo;
        }).collect(Collectors.toList());

        //查询所有附近司机的设置
        List<Long> driverIds = res.stream().map(NearByDriverVo::getDriverId).toList();
        List<DriverSet> driverSets = driverInfoFeignClient.getBatchDriverSet(driverIds).getData();
        Map<Long, DriverSet> driverSetMap = driverSets.stream().collect(Collectors.toMap(DriverSet::getDriverId, s -> s));

        BigDecimal mileageDis = form.getMileageDistance();
        res = res.stream().filter(vo -> {
            DriverSet driverSet = driverSetMap.get(vo.getDriverId());
            //判断司机是否处于接单状态
            if (driverSet == null || driverSet.getServiceStatus() != 1) {
                return false;
            }
            //判断订单路线距离是否大于设置值
            BigDecimal orderDistance = driverSet.getOrderDistance();
            if (orderDistance.compareTo(BigDecimal.ZERO) > 0 && orderDistance.compareTo(mileageDis) < 0) {
                return false;
            };
            //判断离用户距离是否大于设置值
            BigDecimal acceptDistance = driverSet.getAcceptDistance();
            BigDecimal currentDistance = vo.getDistance();
            return acceptDistance.compareTo(BigDecimal.ZERO) <= 0 || acceptDistance.compareTo(currentDistance) >= 0;
        }).toList();

        return res;
    }

    @Override
    public Boolean updateOrderLocationToCache(UpdateOrderLocationForm updateOrderLocationForm) {
        OrderLocationVo orderLocationVo = new OrderLocationVo();
        orderLocationVo.setLatitude(updateOrderLocationForm.getLatitude());
        orderLocationVo.setLongitude(updateOrderLocationForm.getLongitude());
        String key = RedisConstant.UPDATE_ORDER_LOCATION + updateOrderLocationForm.getOrderId();
        // 如果代驾30分钟一直没更新位置，则30分钟自动失效
        redisTemplate.opsForValue().set(key, JSON.toJSONString(orderLocationVo), 30, TimeUnit.MINUTES);
        return true;
    }

    @Override
    public OrderLocationVo getCacheOrderLocation(Long orderId) {
        String key = RedisConstant.UPDATE_ORDER_LOCATION + orderId;
        String value = redisTemplate.opsForValue().get(key);
        return JSON.parseObject(value, OrderLocationVo.class);
    }

    @Override
    public Boolean saveOrderServiceLocation(List<OrderServiceLocationForm> orderLocationServiceFormList) {
        List<OrderServiceLocation> locations = orderLocationServiceFormList.stream().map(form -> {
            OrderServiceLocation loc = new OrderServiceLocation();
            loc.setId(ObjectId.get().toString());
            loc.setOrderId(form.getOrderId());
            loc.setLatitude(form.getLatitude());
            loc.setLongitude(form.getLongitude());
            loc.setCreateTime(new Date());
            return loc;
        }).toList();
        orderServiceLocationRepository.saveAll(locations);
        return true;
    }

    @Override
    public OrderServiceLastLocationVo getOrderServiceLastLocation(Long orderId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("orderId").is(orderId));
        query.with(Sort.by("createTime").descending());
        query.limit(1);
        OrderServiceLocation data = mongoTemplate.findOne(query, OrderServiceLocation.class);

        OrderServiceLastLocationVo vo = new OrderServiceLastLocationVo();
        if (data != null) {
            BeanUtils.copyProperties(data, vo);
        }

        return vo;
    }

    @Override
    public BigDecimal calculateOrderRealDistance(Long orderId) {
        //查询方式1：手动封装查询条件
//        OrderServiceLocation loc = new OrderServiceLocation();
//        loc.setOrderId(orderId);
//        Example<OrderServiceLocation> example = Example.of(loc);
//        List<OrderServiceLocation> route = orderServiceLocationRepository.findAll(
//                example, Sort.by("createTime").ascending());

        //查询方式2：写repository接口
        List<OrderServiceLocation> route = orderServiceLocationRepository.findByOrderIdOrderByCreateTimeAsc(orderId);

        BigDecimal totalDistance = BigDecimal.ZERO;
        for (int i = 1; i < route.size(); i++) {
            OrderServiceLocation prev = route.get(i - 1);
            OrderServiceLocation cur = route.get(i);
            double distance = LocationUtil.getDistance(
                    prev.getLatitude().doubleValue(), prev.getLongitude().doubleValue(),
                    cur.getLatitude().doubleValue(), cur.getLongitude().doubleValue());
            totalDistance = totalDistance.add(BigDecimal.valueOf(distance));
        }

        //TODO 临时制造模拟数据，方便测试
        if(totalDistance.compareTo(BigDecimal.ZERO) == 0) {
            return orderInfoFeignClient.getOrderInfo(orderId).getData().getExpectDistance()
                    .add(new BigDecimal("5"));          //预估距离加上5公里
        }

        return totalDistance;
    }
}
