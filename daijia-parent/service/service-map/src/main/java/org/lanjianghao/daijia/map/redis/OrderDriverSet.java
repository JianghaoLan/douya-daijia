package org.lanjianghao.daijia.map.redis;

import org.lanjianghao.daijia.common.constant.RedisConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class OrderDriverSet {
    @Autowired
    private StringRedisTemplate redisTemplate;

    private String getKey(Long orderId) {
        return RedisConstant.ORDER_DRIVER_SET + orderId;
    }

    public boolean hasDriver(Long orderId, Long driverId) {
        return redisTemplate.opsForSet().isMember(getKey(orderId), driverId.toString());
    }

    public void addDriver(Long orderId, Long driverId) {
        String key = getKey(orderId);
        boolean hasSet = redisTemplate.hasKey(key);
        redisTemplate.opsForSet().add(key, driverId.toString());
        if (!hasSet) {
            redisTemplate.expire(key, RedisConstant.ORDER_DRIVER_SET_EXPIRES_TIME, TimeUnit.MINUTES);
        }
    }

    public void removeSet(Long orderId) {
        redisTemplate.delete(getKey(orderId));
    }
}
