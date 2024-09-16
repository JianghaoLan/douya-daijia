package org.lanjianghao.daijia.order.handler;

import jakarta.annotation.PostConstruct;
import org.lanjianghao.daijia.order.service.OrderInfoService;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

//监听订单延迟队列
@Component
public class RedisDelayHandler {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private OrderInfoService orderInfoService;

    @PostConstruct
    public void listener() {
        new Thread(() -> {
            while (true) {
                RBlockingQueue<String> blockingQueue = redissonClient.getBlockingQueue("queue_cancel_order");

                try {
                    String orderId = blockingQueue.take();
                    if (StringUtils.hasText(orderId)) {
                        orderInfoService.cancelOrder(Long.parseLong(orderId));
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }
}
