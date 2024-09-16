package org.lanjianghao.daijia.driver.service;

import org.lanjianghao.daijia.model.form.map.CalculateDrivingLineForm;
import org.lanjianghao.daijia.model.form.order.OrderFeeForm;
import org.lanjianghao.daijia.model.form.order.StartDriveForm;
import org.lanjianghao.daijia.model.form.order.UpdateOrderCartForm;
import org.lanjianghao.daijia.model.vo.base.PageVo;
import org.lanjianghao.daijia.model.vo.map.DrivingLineVo;
import org.lanjianghao.daijia.model.vo.order.CurrentOrderInfoVo;
import org.lanjianghao.daijia.model.vo.order.NewOrderDataVo;
import org.lanjianghao.daijia.model.vo.order.OrderInfoVo;
import org.lanjianghao.daijia.model.vo.order.OrderListVo;

import java.util.List;

public interface OrderService {


    Integer getOrderStatus(Long orderId);

    List<NewOrderDataVo> findNewOrderQueueData(Long driverId);

    Boolean robNewOrder(Long driverId, Long orderId);

    CurrentOrderInfoVo searchDriverCurrentOrder(Long driverId);

    OrderInfoVo getOrderInfo(Long orderId, Long driverId);

    DrivingLineVo calculateDrivingLine(CalculateDrivingLineForm calculateDrivingLineForm);

    Boolean driverArriveStartLocation(Long orderId, Long driverId);

    Boolean updateOrderCart(UpdateOrderCartForm updateOrderCartForm);

    Boolean startDrive(StartDriveForm startDriveForm);

    Boolean endDrive(OrderFeeForm orderFeeForm);

    PageVo<OrderListVo> findDriverOrderPage(Long driverId, Long page, Long limit);

    Boolean sendOrderBillInfo(Long orderId, Long driverId);
}
