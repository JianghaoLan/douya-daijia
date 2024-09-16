package org.lanjianghao.daijia.map.redis;

import com.alibaba.fastjson2.JSON;
import org.lanjianghao.daijia.common.constant.RedisConstant;
import org.lanjianghao.daijia.common.constant.SystemConstant;
import org.lanjianghao.daijia.model.vo.map.AvailableOrderVo;
import org.lanjianghao.daijia.model.vo.map.OrderLocationInfoVo;
import org.lanjianghao.daijia.model.vo.map.OrderLocationVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.BoundGeoOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderLocationInfoCache {

    private final StringRedisTemplate redisTemplate;

    private final BoundGeoOperations<String, String> orderGeoOps;

    OrderLocationInfoCache(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        String geoKey = RedisConstant.ORDER_GEO_LOCATION;
        orderGeoOps = redisTemplate.boundGeoOps(geoKey);
    }

    private void addGeoLocation(Long orderId, BigDecimal longitude, BigDecimal latitude) {
        orderGeoOps.add(new Point(longitude.doubleValue(), latitude.doubleValue()), orderId.toString());
    }

    private void removeGeoLocation(Long orderId) {
        orderGeoOps.remove(orderId.toString());
    }

    private void setOrderLocationInfo(OrderLocationInfoVo vo) {
        redisTemplate.opsForValue().set(RedisConstant.ORDER_LOCATION_INFO + vo.getOrderId(), JSON.toJSONString(vo));
    }

    private void removeOrderLocationInfo(Long orderId) {
        redisTemplate.delete(RedisConstant.ORDER_LOCATION_INFO + orderId);
    }

    private OrderLocationInfoVo getOrderLocationInfo(Long orderId) {
        String value = redisTemplate.opsForValue().get(RedisConstant.ORDER_LOCATION_INFO + orderId);
        return JSON.parseObject(value, OrderLocationInfoVo.class);
    }

    public void set(OrderLocationInfoVo vo) {
        Long orderId = vo.getOrderId();
        setOrderLocationInfo(vo);
        addGeoLocation(orderId, vo.getStartPointLongitude(), vo.getStartPointLatitude());
    }

    public void remove(Long orderId) {
        removeOrderLocationInfo(orderId);
        removeGeoLocation(orderId);
    }

    public List<AvailableOrderVo> searchAvailableOrders(BigDecimal longitude, BigDecimal latitude,
                                                        BigDecimal acceptDistance, BigDecimal orderDistance) {
        RedisGeoCommands.GeoRadiusCommandArgs args =
                RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                        .includeDistance()
                        .includeCoordinates()
                        .sortAscending();
        GeoResults<RedisGeoCommands.GeoLocation<String>> geoRes = orderGeoOps.search(
                GeoReference.fromCoordinate(longitude.doubleValue(), latitude.doubleValue()),
                new Distance(acceptDistance.doubleValue(), Metrics.KILOMETERS),
                args);

        if (geoRes == null || CollectionUtils.isEmpty(geoRes.getContent())) {
            return Collections.emptyList();
        }

        return geoRes.getContent().stream().map(item -> {
            AvailableOrderVo vo = new AvailableOrderVo();
            vo.setOrderId(Long.parseLong(item.getContent().getName()));
            BigDecimal distance = getKmBigDecimal(item.getDistance());
            vo.setDistance(distance);
            return vo;
        }).filter(vo -> {
            if (orderDistance.compareTo(BigDecimal.ZERO) <= 0) {
                return true;
            }
            OrderLocationInfoVo orderLocationInfo = this.getOrderLocationInfo(vo.getOrderId());
            return orderLocationInfo.getExpectDistance().compareTo(orderDistance) <= 0;
        }).toList();
    }

    private BigDecimal getKmBigDecimal(Distance distance) {
        return BigDecimal.valueOf(distance.getNormalizedValue())
                .multiply(BigDecimal.valueOf(Metrics.KILOMETERS.getMultiplier()))
                .setScale(2, RoundingMode.HALF_UP);
    }

}
