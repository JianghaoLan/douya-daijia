package org.lanjianghao.daijia.order.redis;

import com.alibaba.fastjson.JSON;
import org.lanjianghao.daijia.common.constant.RedisConstant;
import org.lanjianghao.daijia.common.constant.SystemConstant;
import org.lanjianghao.daijia.model.entity.order.OrderInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class PendingOrderCache {
    @Autowired
    private StringRedisTemplate redisTemplate;

    private String getKey(Long orderId) {
        return RedisConstant.ORDER_PENDING_SET + orderId;
    }

    public void set(OrderInfo orderInfo) {
        redisTemplate.opsForValue().set(getKey(orderInfo.getId()), JSON.toJSONString(orderInfo),
                SystemConstant.ORDER_EXPIRES_TIME, TimeUnit.MINUTES);
    }

    public OrderInfo get(Long orderId) {
        String value = redisTemplate.opsForValue().get(getKey(orderId));
        return JSON.parseObject(value, OrderInfo.class);
    }

    public void remove(Long orderId) {
        redisTemplate.delete(getKey(orderId));
    }

    public List<OrderInfo> getAll(List<Long> orderIds) {
        List<String> keys = orderIds.stream().map(this::getKey).toList();
        List<String> values = redisTemplate.opsForValue().multiGet(keys);
        if (values == null) {
            return null;
        }
        return values.stream().map(value -> JSON.parseObject(value, OrderInfo.class)).toList();
    }
}
