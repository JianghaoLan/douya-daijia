package org.lanjianghao.daijia.map.service.impl;

import org.lanjianghao.daijia.common.constant.RedisConstant;
import org.lanjianghao.daijia.common.constant.SystemConstant;
import org.lanjianghao.daijia.common.result.Result;
import org.lanjianghao.daijia.driver.client.DriverInfoFeignClient;
import org.lanjianghao.daijia.map.service.LocationService;
import lombok.extern.slf4j.Slf4j;
import org.lanjianghao.daijia.model.entity.driver.DriverSet;
import org.lanjianghao.daijia.model.form.map.SearchNearByDriverForm;
import org.lanjianghao.daijia.model.form.map.UpdateDriverLocationForm;
import org.lanjianghao.daijia.model.vo.map.NearByDriverVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.*;
import org.springframework.data.geo.format.DistanceFormatter;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class LocationServiceImpl implements LocationService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private DriverInfoFeignClient driverInfoFeignClient;

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
}
