package org.lanjianghao.daijia.order.service;

import org.lanjianghao.daijia.model.entity.order.OrderInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import org.lanjianghao.daijia.model.form.order.OrderInfoForm;

public interface OrderInfoService extends IService<OrderInfo> {

    Long saveOrderInfo(OrderInfoForm orderInfoForm);

    Integer getOrderStatus(Long orderId);
}
