package org.lanjianghao.daijia.order.service.impl;

import org.lanjianghao.daijia.model.entity.order.OrderInfo;
import org.lanjianghao.daijia.model.entity.order.OrderStatusLog;
import org.lanjianghao.daijia.model.enums.OrderStatus;
import org.lanjianghao.daijia.model.form.order.OrderInfoForm;
import org.lanjianghao.daijia.order.mapper.OrderInfoMapper;
import org.lanjianghao.daijia.order.mapper.OrderStatusLogMapper;
import org.lanjianghao.daijia.order.service.OrderInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderInfoService {

    @Autowired
    private OrderStatusLogMapper orderStatusLogMapper;

    @Override
    public Long saveOrderInfo(OrderInfoForm orderInfoForm) {
        OrderInfo orderInfo = new OrderInfo();
        BeanUtils.copyProperties(orderInfoForm, orderInfo);
        orderInfo.setOrderNo(UUID.randomUUID().toString().replace("-", ""));
        orderInfo.setStatus(OrderStatus.WAITING_ACCEPT.getStatus());

        this.save(orderInfo);

        //记录日志
        OrderStatusLog logEntity = new OrderStatusLog();
        logEntity.setOrderId(orderInfo.getId());
        logEntity.setOrderStatus(OrderStatus.WAITING_ACCEPT.getStatus());
        logEntity.setOperateTime(new Date());
        orderStatusLogMapper.insert(logEntity);

        return orderInfo.getId();
    }

    @Override
    public Integer getOrderStatus(Long orderId) {
        Integer status = this.baseMapper.selectStatusById(orderId);
        if (status == null) {
            return OrderStatus.NULL_ORDER.getStatus();
        }
        return status;
    }
}
