package org.lanjianghao.daijia.driver.service.impl;

import org.lanjianghao.daijia.driver.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.lanjianghao.daijia.order.client.OrderInfoFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderInfoFeignClient orderInfoFeignClient;

    @Override
    public Integer getOrderStatus(Long orderId) {
        return orderInfoFeignClient.getOrderStatus(orderId).getData();
    }
}
