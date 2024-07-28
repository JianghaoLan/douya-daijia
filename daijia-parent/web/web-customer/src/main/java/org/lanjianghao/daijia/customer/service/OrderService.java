package org.lanjianghao.daijia.customer.service;

import org.lanjianghao.daijia.model.form.customer.ExpectOrderForm;
import org.lanjianghao.daijia.model.form.customer.SubmitOrderForm;
import org.lanjianghao.daijia.model.form.map.CalculateDrivingLineForm;
import org.lanjianghao.daijia.model.vo.customer.ExpectOrderVo;
import org.lanjianghao.daijia.model.vo.driver.DriverInfoVo;
import org.lanjianghao.daijia.model.vo.map.DrivingLineVo;
import org.lanjianghao.daijia.model.vo.map.OrderLocationVo;
import org.lanjianghao.daijia.model.vo.map.OrderServiceLastLocationVo;
import org.lanjianghao.daijia.model.vo.order.CurrentOrderInfoVo;
import org.lanjianghao.daijia.model.vo.order.OrderInfoVo;

public interface OrderService {

    ExpectOrderVo expectOrder(ExpectOrderForm expectOrderForm);

    Long submitOrder(SubmitOrderForm submitOrderForm);

    Integer getOrderStatus(Long orderId);

    CurrentOrderInfoVo searchCustomerCurrentOrder(Long customerId);

    OrderInfoVo getOrderInfo(Long orderId, Long customerId);

    DriverInfoVo getDriverInfo(Long orderId, Long customerId);

    OrderLocationVo getCacheOrderLocation(Long orderId, Long customerId);

    DrivingLineVo calculateDrivingLine(CalculateDrivingLineForm calculateDrivingLineForm);

    OrderServiceLastLocationVo getOrderServiceLastLocation(Long orderId);
}
