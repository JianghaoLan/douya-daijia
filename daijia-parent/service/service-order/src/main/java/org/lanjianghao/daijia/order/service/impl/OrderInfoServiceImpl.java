package org.lanjianghao.daijia.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.lanjianghao.daijia.common.constant.RedisConstant;
import org.lanjianghao.daijia.common.execption.BusinessException;
import org.lanjianghao.daijia.common.result.ResultCodeEnum;
import org.lanjianghao.daijia.model.entity.order.OrderInfo;
import org.lanjianghao.daijia.model.entity.order.OrderStatusLog;
import org.lanjianghao.daijia.model.enums.OrderStatus;
import org.lanjianghao.daijia.model.form.order.OrderInfoForm;
import org.lanjianghao.daijia.model.form.order.StartDriveForm;
import org.lanjianghao.daijia.model.form.order.UpdateOrderCartForm;
import org.lanjianghao.daijia.model.vo.order.CurrentOrderInfoVo;
import org.lanjianghao.daijia.order.mapper.OrderInfoMapper;
import org.lanjianghao.daijia.order.mapper.OrderStatusLogMapper;
import org.lanjianghao.daijia.order.service.OrderInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderInfoService {

    @Autowired
    private OrderStatusLogMapper orderStatusLogMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    final private static Integer[] CustomerOrderActiveStatus = {
            OrderStatus.WAITING_ACCEPT.getStatus(),     //TODO 是否包含这个状态
            OrderStatus.ACCEPTED.getStatus(),
            OrderStatus.DRIVER_ARRIVED.getStatus(),
            OrderStatus.UPDATE_CART_INFO.getStatus(),
            OrderStatus.START_SERVICE.getStatus(),
            OrderStatus.END_SERVICE.getStatus(),
            OrderStatus.UNPAID.getStatus()
    };

    final private static Integer[] DriverOrderActiveStatus = {
            OrderStatus.ACCEPTED.getStatus(),
            OrderStatus.DRIVER_ARRIVED.getStatus(),
            OrderStatus.UPDATE_CART_INFO.getStatus(),
            OrderStatus.START_SERVICE.getStatus(),
            OrderStatus.END_SERVICE.getStatus(),
            OrderStatus.UNPAID.getStatus()
    };

    @Override
    public Long saveOrderInfo(OrderInfoForm orderInfoForm) {
        OrderInfo orderInfo = new OrderInfo();
        BeanUtils.copyProperties(orderInfoForm, orderInfo);
        orderInfo.setOrderNo(UUID.randomUUID().toString().replace("-", ""));
        orderInfo.setStatus(OrderStatus.WAITING_ACCEPT.getStatus());
        this.save(orderInfo);

        //向redis添加标识
        redisTemplate.opsForValue().set(RedisConstant.ORDER_ACCEPT_MARK + orderInfo.getId(), "0",
                RedisConstant.ORDER_ACCEPT_MARK_EXPIRES_TIME, TimeUnit.MINUTES);

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

    private boolean setOrderReceivedInDb(Long orderId, Long driverId) {
        OrderInfo info = new OrderInfo();
        info.setId(orderId);
        info.setDriverId(driverId);
        info.setStatus(OrderStatus.ACCEPTED.getStatus());
        info.setAcceptTime(new Date());
        return this.updateById(info);
    }

    @Override
    public Boolean robNewOrder(Long driverId, Long orderId) {
        String orderRedisKey = RedisConstant.ORDER_ACCEPT_MARK + orderId;
        if (!redisTemplate.hasKey(orderRedisKey)) {
            //已经被抢单
            throw new BusinessException(ResultCodeEnum.COB_NEW_ORDER_FAIL);
        }

        RLock lock = redissonClient.getLock(RedisConstant.ROB_NEW_ORDER_LOCK + orderId);
        boolean flag = false;

        try {
            flag = lock.tryLock(
                    RedisConstant.ROB_NEW_ORDER_LOCK_WAIT_TIME,
                    RedisConstant.ROB_NEW_ORDER_LOCK_LEASE_TIME, TimeUnit.SECONDS);
            if (flag) {
                if (!redisTemplate.hasKey(orderRedisKey)) {
                    //已经被抢单
                    throw new BusinessException(ResultCodeEnum.COB_NEW_ORDER_FAIL);
                }

                ///删除Redis中订单标识
                redisTemplate.delete(orderRedisKey);

                //修改数据库中订单状态
                boolean updateOrderSuccess = setOrderReceivedInDb(orderId, driverId);
                if (!updateOrderSuccess) {
                    throw new BusinessException(ResultCodeEnum.COB_NEW_ORDER_FAIL);
                }

                return true;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (flag) {
                lock.unlock();
            }
        }

        throw new BusinessException(ResultCodeEnum.COB_NEW_ORDER_FAIL);
    }

    @Override
    public CurrentOrderInfoVo searchCustomerCurrentOrder(Long customerId) {
        LambdaQueryWrapper<OrderInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderInfo::getCustomerId, customerId)
                .in(OrderInfo::getStatus, Arrays.asList(CustomerOrderActiveStatus))
                .orderByDesc(OrderInfo::getId)
                .last(" LIMIT 1");
        OrderInfo info = this.getOne(wrapper);

        CurrentOrderInfoVo vo = new CurrentOrderInfoVo();
        if (info != null) {
            vo.setOrderId(info.getId());
            vo.setStatus(info.getStatus());
            vo.setIsHasCurrentOrder(true);
        } else {
            vo.setIsHasCurrentOrder(false);
        }

        return vo;
    }

    @Override
    public CurrentOrderInfoVo searchDriverCurrentOrder(Long driverId) {
        LambdaQueryWrapper<OrderInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderInfo::getDriverId, driverId)
                .in(OrderInfo::getStatus, Arrays.asList(DriverOrderActiveStatus))
                .orderByDesc(OrderInfo::getId)
                .last(" LIMIT 1");
        OrderInfo info = this.getOne(wrapper);

        CurrentOrderInfoVo vo = new CurrentOrderInfoVo();
        if (info != null) {
            vo.setOrderId(info.getId());
            vo.setStatus(info.getStatus());
            vo.setIsHasCurrentOrder(true);
        } else {
            vo.setIsHasCurrentOrder(false);
        }

        return vo;
    }

    @Override
    public Boolean driverArriveStartLocation(Long orderId, Long driverId) {
        OrderInfo forUpdate = new OrderInfo();
        forUpdate.setStatus(OrderStatus.DRIVER_ARRIVED.getStatus());
        forUpdate.setArriveTime(new Date());

        LambdaQueryWrapper<OrderInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderInfo::getId, orderId);
        wrapper.eq(OrderInfo::getDriverId, driverId);
        boolean success = this.update(forUpdate, wrapper);
        if (success) {
            return true;
        }
        throw new BusinessException(ResultCodeEnum.UPDATE_ERROR);
    }

    @Override
    public Boolean updateOrderCart(UpdateOrderCartForm form) {
        OrderInfo forUpdate = new OrderInfo();
        forUpdate.setCarLicense(form.getCarLicense());
        forUpdate.setCarType(form.getCarType());
        forUpdate.setCarFrontUrl(form.getCarFrontUrl());
        forUpdate.setCarBackUrl(form.getCarBackUrl());
        forUpdate.setStatus(OrderStatus.UPDATE_CART_INFO.getStatus());

        LambdaQueryWrapper<OrderInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderInfo::getId, form.getOrderId())
                .eq(OrderInfo::getDriverId, form.getDriverId());
        boolean success = this.update(forUpdate, wrapper);
        if (success) {
            return true;
        }
        throw new BusinessException(ResultCodeEnum.UPDATE_ERROR);
    }

    private boolean setOrderStartDriveInDb(Long orderId, Long driverId) {
        LambdaQueryWrapper<OrderInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderInfo::getId, orderId).eq(OrderInfo::getDriverId, driverId);
        OrderInfo forUpdate = new OrderInfo();
        forUpdate.setStatus(OrderStatus.START_SERVICE.getStatus());
        forUpdate.setStartServiceTime(new Date());
        return this.update(forUpdate, wrapper);
    }

    @Override
    public Boolean startDrive(StartDriveForm form) {
        if (setOrderStartDriveInDb(form.getOrderId(), form.getDriverId())) {
            return true;
        }
        throw new BusinessException(ResultCodeEnum.UPDATE_ERROR);
    }
}
