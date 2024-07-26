package org.lanjianghao.daijia.driver.service;

import org.lanjianghao.daijia.model.vo.order.NewOrderDataVo;

import java.util.List;

public interface OrderService {


    Integer getOrderStatus(Long orderId);

    List<NewOrderDataVo> findNewOrderQueueData(Long driverId);
}
