package org.lanjianghao.daijia.map.redis;

import com.alibaba.fastjson.JSON;
import lombok.Data;
import org.lanjianghao.daijia.common.constant.RedisConstant;
import org.lanjianghao.daijia.model.vo.map.LocationVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;


@Data
@Component
public class DriverLocationCache {
    @Autowired
    private StringRedisTemplate redisTemplate;

    private String getKey(Long driverId) {
        return RedisConstant.DRIVER_LOCATION + driverId;
    }

    private long getExpires() {
        return RedisConstant.DRIVER_LOCATION_EXPIRES_TIME;
    }

    public void add(Long driverId, LocationVo location) {
        redisTemplate.opsForValue().set(getKey(driverId), JSON.toJSONString(location), getExpires(), TimeUnit.MINUTES);
    }

    public LocationVo get(Long driverId) {
        String value = redisTemplate.opsForValue().get(getKey(driverId));
        return JSON.parseObject(value, LocationVo.class);
    }

    public void remove(Long driverId) {
        redisTemplate.delete(getKey(driverId));
    }
}
