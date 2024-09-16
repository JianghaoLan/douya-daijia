package org.lanjianghao.daijia.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.lanjianghao.daijia.common.constant.RedisConstant;
import org.lanjianghao.daijia.common.constant.SystemConstant;
import org.lanjianghao.daijia.common.execption.BusinessException;
import org.lanjianghao.daijia.common.result.ResultCodeEnum;
import org.lanjianghao.daijia.model.entity.order.OrderBill;
import org.lanjianghao.daijia.model.entity.order.OrderInfo;
import org.lanjianghao.daijia.model.entity.order.OrderProfitsharing;
import org.lanjianghao.daijia.model.entity.order.OrderStatusLog;
import org.lanjianghao.daijia.model.enums.OrderStatus;
import org.lanjianghao.daijia.model.form.order.OrderInfoForm;
import org.lanjianghao.daijia.model.form.order.StartDriveForm;
import org.lanjianghao.daijia.model.form.order.UpdateOrderBillForm;
import org.lanjianghao.daijia.model.form.order.UpdateOrderCartForm;
import org.lanjianghao.daijia.model.vo.order.*;
import org.lanjianghao.daijia.order.mapper.OrderBillMapper;
import org.lanjianghao.daijia.order.mapper.OrderInfoMapper;
import org.lanjianghao.daijia.order.mapper.OrderProfitsharingMapper;
import org.lanjianghao.daijia.order.mapper.OrderStatusLogMapper;
import org.lanjianghao.daijia.order.redis.PendingOrderCache;
import org.lanjianghao.daijia.order.service.OrderInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
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

    @Autowired
    private OrderBillMapper orderBillMapper;

    @Autowired
    private OrderProfitsharingMapper orderProfitsharingMapper;

    @Autowired
    private PendingOrderCache pendingOrderCache;

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
        pendingOrderCache.set(orderInfo);

        //发送延迟消息，时间到后自动取消订单
        this.sendDelayMessage(orderInfo.getId());

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
    public List<OrderInfo> getPendingOrderInfos(List<Long> orderIds) {
        return pendingOrderCache.getAll(orderIds);
    }

    private void sendDelayMessage(Long orderId) {
        try {
            RBlockingQueue<Object> blockingQueue = redissonClient.getBlockingQueue("queue_cancel_order");
            RDelayedQueue<Object> delayedQueue = redissonClient.getDelayedQueue(blockingQueue);

            delayedQueue.offer(orderId.toString(), SystemConstant.ORDER_EXPIRES_TIME, TimeUnit.MINUTES);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(ResultCodeEnum.DATA_ERROR);
        }
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

                //删除订单信息redis缓存
                pendingOrderCache.remove(orderId);

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

    @Override
    public Long getOrderNumByTime(String startTime, String endTime) {
        LambdaQueryWrapper<OrderInfo> query = new LambdaQueryWrapper<>();
        query.ge(OrderInfo::getStartServiceTime, startTime)
                .lt(OrderInfo::getStartServiceTime, endTime);

        return this.count(query);
    }

    private boolean setOrderEndDriveInDb(Long orderId, Long driverId,
                                         BigDecimal realAmount, BigDecimal realDistance, BigDecimal favourFee) {
        LambdaQueryWrapper<OrderInfo> query = new LambdaQueryWrapper<>();
        query.eq(OrderInfo::getId, orderId).eq(OrderInfo::getDriverId, driverId);

        OrderInfo forUpdate = new OrderInfo();
        forUpdate.setStatus(OrderStatus.END_SERVICE.getStatus());
        forUpdate.setEndServiceTime(new Date());
        forUpdate.setRealAmount(realAmount);
        forUpdate.setRealDistance(realDistance);
        forUpdate.setFavourFee(favourFee);
        return this.update(forUpdate, query);
    }

    @Override
    public Boolean endDrive(UpdateOrderBillForm form) {
        boolean ret = setOrderEndDriveInDb(form.getOrderId(), form.getDriverId(),
                form.getTotalAmount(), form.getRealDistance(), form.getFavourFee());
        if (!ret) {
            throw new BusinessException(ResultCodeEnum.UPDATE_ERROR);
        }

        //插入实际账单数据
        OrderBill orderBill = new OrderBill();
        BeanUtils.copyProperties(form, orderBill);
        orderBill.setPayAmount(form.getTotalAmount());
        orderBillMapper.insert(orderBill);

        //插入分账信息数据
        OrderProfitsharing orderProfitsharing = new OrderProfitsharing();
        BeanUtils.copyProperties(form, orderProfitsharing);
        orderProfitsharing.setOrderId(form.getOrderId());
        orderProfitsharing.setRuleId(form.getProfitsharingRuleId());
        orderProfitsharing.setStatus(1);
        orderProfitsharingMapper.insert(orderProfitsharing);

        return true;
    }

    @Override
    public IPage<OrderListVo> pageCustomerOrderByCustomerId(Long customerId, Page<OrderListVo> pageParam) {
        return this.baseMapper.selectCustomerOrderPageByCustomerId(customerId, pageParam);
    }

    @Override
    public IPage<OrderListVo> pageDriverOrderByDriverId(Long driverId, Page<OrderListVo> pageParam) {
        return this.baseMapper.selectDriverOrderPageByDriverId(driverId, pageParam);
    }

    @Override
    public OrderBillVo getOrderBillInfo(Long orderId) {
        LambdaQueryWrapper<OrderBill> query = new LambdaQueryWrapper<>();
        query.eq(OrderBill::getOrderId, orderId);
        OrderBill orderBill = this.orderBillMapper.selectOne(query);

        OrderBillVo vo = new OrderBillVo();
        BeanUtils.copyProperties(orderBill, vo);
        return vo;
    }

    @Override
    public OrderProfitsharingVo getOrderProfitsharing(Long orderId) {
        LambdaQueryWrapper<OrderProfitsharing> query = new LambdaQueryWrapper<>();
        query.eq(OrderProfitsharing::getOrderId, orderId);
        OrderProfitsharing orderProfitsharing = this.orderProfitsharingMapper.selectOne(query);

        OrderProfitsharingVo vo = new OrderProfitsharingVo();
        BeanUtils.copyProperties(orderProfitsharing, vo);
        return vo;
    }

    @Override
    public Boolean sendOrderBillInfo(Long orderId, Long driverId) {
        LambdaQueryWrapper<OrderInfo> query = new LambdaQueryWrapper<>();
        query.eq(OrderInfo::getId, orderId)
                .eq(OrderInfo::getDriverId, driverId)
                .eq(OrderInfo::getStatus, OrderStatus.END_SERVICE.getStatus());

        OrderInfo forUpdate = new OrderInfo();
        forUpdate.setStatus(OrderStatus.UNPAID.getStatus());
        boolean success = this.update(forUpdate, query);
        if (success) {
            return true;
        }
        throw new BusinessException(ResultCodeEnum.UPDATE_ERROR);
    }

    @Override
    public OrderPayVo getOrderPayVo(String orderNo, Long customerId) {
        OrderPayVo vo = this.baseMapper.selectOrderPayVoByOrderNoAndCustomerId(orderNo, customerId);
        if (vo != null) {
            vo.setContent(vo.getStartLocation() + " 到 " + vo.getEndLocation());
        }
        return vo;
    }

    @Override
    public Boolean updateOrderPayStatus(String orderNo) {
        LambdaQueryWrapper<OrderInfo> query = new LambdaQueryWrapper<>();
        query.eq(OrderInfo::getOrderNo, orderNo);
        query.ne(OrderInfo::getStatus, OrderStatus.PAID.getStatus());
        OrderInfo forUpdate = new OrderInfo();
        forUpdate.setStatus(OrderStatus.PAID.getStatus());
        forUpdate.setPayTime(new Date());
        return this.update(forUpdate, query);
    }

    private OrderInfo getByOrderNo(String orderNo) {
        LambdaQueryWrapper<OrderInfo> query = new LambdaQueryWrapper<>();
        return this.getOne(query);
    }

    @Override
    public OrderRewardVo getOrderRewardFee(String orderNo) {
        OrderInfo orderInfo = this.getByOrderNo(orderNo);
        Long orderId = orderInfo.getId();
        Long driverId = orderInfo.getDriverId();

        OrderBill orderBill = orderBillMapper.selectOne(
                new LambdaQueryWrapper<OrderBill>().eq(OrderBill::getOrderId, orderId));

        OrderRewardVo vo = new OrderRewardVo();
        vo.setOrderId(orderId);
        vo.setDriverId(driverId);
        vo.setRewardFee(orderBill.getRewardFee());
        return vo;
    }

    private boolean cancelOrderInDb(long orderId) {
        LambdaQueryWrapper<OrderInfo> query = new LambdaQueryWrapper<>();
        query.eq(OrderInfo::getId, orderId)
                .eq(OrderInfo::getStatus, OrderStatus.WAITING_ACCEPT.getStatus());
        OrderInfo forUpdate = new OrderInfo();
        forUpdate.setStatus(OrderStatus.CANCEL_ORDER.getStatus());
        return this.update(forUpdate, query);
    }

    private boolean cancelOrderInDb(long orderId, long customerId) {
        LambdaQueryWrapper<OrderInfo> query = new LambdaQueryWrapper<>();
        query.eq(OrderInfo::getId, orderId)
                .eq(OrderInfo::getStatus, OrderStatus.WAITING_ACCEPT.getStatus())
                .eq(OrderInfo::getCustomerId, customerId);
        OrderInfo forUpdate = new OrderInfo();
        forUpdate.setStatus(OrderStatus.CANCEL_ORDER.getStatus());
        return this.update(forUpdate, query);
    }

    @Override
    public void cancelOrder(long orderId) {
        boolean success = cancelOrderInDb(orderId);

        if (!success) {
            throw new BusinessException(ResultCodeEnum.ILLEGAL_REQUEST);
        }

        //删除接单标识
        String orderRedisKey = RedisConstant.ORDER_ACCEPT_MARK + orderId;
        redisTemplate.delete(orderRedisKey);

        //删除待接单订单信息缓存
        pendingOrderCache.remove(orderId);
    }

    @Override
    public void cancelOrder(Long orderId, Long customerId) {
        boolean success = cancelOrderInDb(orderId, customerId);

        if (!success) {
            throw new BusinessException(ResultCodeEnum.ILLEGAL_REQUEST);
        }

        //删除接单标识
        String orderRedisKey = RedisConstant.ORDER_ACCEPT_MARK + orderId;
        redisTemplate.delete(orderRedisKey);

        //删除待接单订单信息缓存
        pendingOrderCache.remove(orderId);
    }

    @Override
    public Boolean updateCouponAmount(Long orderId, BigDecimal couponAmount) {
        int rows = orderBillMapper.updateCouponAmount(orderId, couponAmount);
        return rows > 0;
    }

}
