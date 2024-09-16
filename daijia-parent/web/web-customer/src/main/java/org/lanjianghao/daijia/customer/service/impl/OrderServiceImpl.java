package org.lanjianghao.daijia.customer.service.impl;

import io.seata.spring.annotation.GlobalTransactional;
import org.lanjianghao.daijia.common.execption.BusinessException;
import org.lanjianghao.daijia.common.result.Result;
import org.lanjianghao.daijia.common.result.ResultCodeEnum;
import org.lanjianghao.daijia.coupon.client.CouponFeignClient;
import org.lanjianghao.daijia.customer.client.CustomerInfoFeignClient;
import org.lanjianghao.daijia.customer.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.lanjianghao.daijia.dispatch.client.NewOrderFeignClient;
import org.lanjianghao.daijia.driver.client.DriverInfoFeignClient;
import org.lanjianghao.daijia.map.client.LocationFeignClient;
import org.lanjianghao.daijia.map.client.MapFeignClient;
import org.lanjianghao.daijia.map.client.WxPayFeignClient;
import org.lanjianghao.daijia.model.entity.order.OrderInfo;
import org.lanjianghao.daijia.model.enums.OrderStatus;
import org.lanjianghao.daijia.model.form.coupon.UseCouponForm;
import org.lanjianghao.daijia.model.form.customer.ExpectOrderForm;
import org.lanjianghao.daijia.model.form.customer.SubmitOrderForm;
import org.lanjianghao.daijia.model.form.map.CalculateDrivingLineForm;
import org.lanjianghao.daijia.model.form.order.OrderInfoForm;
import org.lanjianghao.daijia.model.form.payment.CreateWxPaymentForm;
import org.lanjianghao.daijia.model.form.payment.PaymentInfoForm;
import org.lanjianghao.daijia.model.form.rules.FeeRuleRequestForm;
import org.lanjianghao.daijia.model.vo.base.PageVo;
import org.lanjianghao.daijia.model.vo.customer.ExpectOrderVo;
import org.lanjianghao.daijia.model.vo.driver.DriverInfoVo;
import org.lanjianghao.daijia.model.vo.map.*;
import org.lanjianghao.daijia.model.vo.order.*;
import org.lanjianghao.daijia.model.vo.payment.WxPrepayVo;
import org.lanjianghao.daijia.model.vo.rules.FeeRuleResponseVo;
import org.lanjianghao.daijia.order.client.OrderInfoFeignClient;
import org.lanjianghao.daijia.rules.client.FeeRuleFeignClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;

@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class OrderServiceImpl implements OrderService {

    @Autowired
    private MapFeignClient mapFeignClient;

    @Autowired
    private FeeRuleFeignClient feeRuleFeignClient;

    @Autowired
    private OrderInfoFeignClient orderInfoFeignClient;

    @Autowired
    private NewOrderFeignClient newOrderFeignClient;

    @Autowired
    private DriverInfoFeignClient driverInfoFeignClient;

    @Autowired
    private LocationFeignClient locationFeignClient;

    @Autowired
    private WxPayFeignClient wxPayFeignClient;

    @Autowired
    private CustomerInfoFeignClient customerInfoFeignClient;

    @Autowired
    private CouponFeignClient couponFeignClient;

    @Override
    public ExpectOrderVo expectOrder(ExpectOrderForm expectOrderForm) {
        //计算路线
        CalculateDrivingLineForm calculateDrivingLineForm = new CalculateDrivingLineForm();
        BeanUtils.copyProperties(expectOrderForm, calculateDrivingLineForm);
        DrivingLineVo route = mapFeignClient.calculateDrivingLine(calculateDrivingLineForm).getData();

        //计算预估费用
        FeeRuleRequestForm feeRuleRequestForm = new FeeRuleRequestForm();
        feeRuleRequestForm.setDistance(route.getDistance());
        feeRuleRequestForm.setStartTime(new Date());
        feeRuleRequestForm.setWaitMinute(0);
        FeeRuleResponseVo feeRule = feeRuleFeignClient.calculateOrderFee(feeRuleRequestForm).getData();

        ExpectOrderVo res = new ExpectOrderVo();
        res.setDrivingLineVo(route);
        res.setFeeRuleResponseVo(feeRule);

        return res;
    }

    @Override
    public Long submitOrder(SubmitOrderForm submitOrderForm) {
        //重新计算路线和价格
        CalculateDrivingLineForm calculateDrivingLineForm = new CalculateDrivingLineForm();
        BeanUtils.copyProperties(submitOrderForm, calculateDrivingLineForm);
        DrivingLineVo drivingLineVo = mapFeignClient.calculateDrivingLine(calculateDrivingLineForm).getData();

        //重新计算预估费用
        FeeRuleRequestForm feeRuleRequestForm = new FeeRuleRequestForm();
        feeRuleRequestForm.setDistance(drivingLineVo.getDistance());
        feeRuleRequestForm.setStartTime(new Date());
        feeRuleRequestForm.setWaitMinute(0);
        FeeRuleResponseVo feeRule = feeRuleFeignClient.calculateOrderFee(feeRuleRequestForm).getData();

        OrderInfoForm orderInfoForm = new OrderInfoForm();
        BeanUtils.copyProperties(submitOrderForm, orderInfoForm);
        orderInfoForm.setExpectDistance(drivingLineVo.getDistance());
        orderInfoForm.setExpectAmount(feeRule.getTotalAmount());
        Result<Long> res = orderInfoFeignClient.saveOrderInfo(orderInfoForm);
        Long orderId = res.getData();

        //上传订单开始位置
//        OrderStartLocationVo orderStartLocationVo = new OrderStartLocationVo();
//        orderStartLocationVo.setOrderId(orderId);
//        orderStartLocationVo.setLatitude(orderInfoForm.getStartPointLongitude());
//        orderStartLocationVo.setLongitude(orderInfoForm.getStartPointLongitude());

        OrderLocationInfoVo locationInfo = new OrderLocationInfoVo();
        BeanUtils.copyProperties(submitOrderForm, locationInfo);
        locationInfo.setExpectDistance(drivingLineVo.getDistance());
        locationInfo.setOrderId(orderId);
        locationFeignClient.setOrderLocationInfo(locationInfo);

//        //远程调用定时查询附近可以接单的司机
//        NewOrderTaskVo newOrderTaskVo = new NewOrderTaskVo();
//        newOrderTaskVo.setOrderId(orderId);
//        newOrderTaskVo.setStartLocation(orderInfoForm.getStartLocation());
//        newOrderTaskVo.setStartPointLongitude(orderInfoForm.getStartPointLongitude());
//        newOrderTaskVo.setStartPointLatitude(orderInfoForm.getStartPointLatitude());
//        newOrderTaskVo.setEndLocation(orderInfoForm.getEndLocation());
//        newOrderTaskVo.setEndPointLongitude(orderInfoForm.getEndPointLongitude());
//        newOrderTaskVo.setEndPointLatitude(orderInfoForm.getEndPointLatitude());
//        newOrderTaskVo.setExpectAmount(orderInfoForm.getExpectAmount());
//        newOrderTaskVo.setExpectDistance(orderInfoForm.getExpectDistance());
//        newOrderTaskVo.setExpectTime(drivingLineVo.getDuration());
//        newOrderTaskVo.setFavourFee(orderInfoForm.getFavourFee());
//        Long jobId = newOrderFeignClient.addAndStartTask(newOrderTaskVo).getData();

        return orderId;
    }

    @Override
    public Integer getOrderStatus(Long orderId) {
        return orderInfoFeignClient.getOrderStatus(orderId).getData();
    }

    @Override
    public CurrentOrderInfoVo searchCustomerCurrentOrder(Long customerId) {
        return orderInfoFeignClient.searchCustomerCurrentOrder(customerId).getData();
    }

    @Override
    public OrderInfoVo getOrderInfo(Long orderId, Long customerId) {
        OrderInfo info = orderInfoFeignClient.getOrderInfo(orderId).getData();
        if (info.getCustomerId().intValue() != customerId) {
            throw new BusinessException(ResultCodeEnum.DATA_ERROR);
        }

        DriverInfoVo driverInfoVo = new DriverInfoVo();
        if (info.getDriverId() != null) {
            driverInfoVo = driverInfoFeignClient.getDriverInfo(info.getDriverId()).getData();
        }

        //获取账单信息
        OrderBillVo orderBillVo = new OrderBillVo();
        if (info.getStatus() >= OrderStatus.UNPAID.getStatus()) {
            orderBillVo = orderInfoFeignClient.getOrderBillInfo(orderId).getData();
        }

        OrderInfoVo vo = new OrderInfoVo();
        BeanUtils.copyProperties(info, vo);
        vo.setOrderId(info.getId());
        vo.setOrderBillVo(orderBillVo);
        vo.setDriverInfoVo(driverInfoVo);
        return vo;
    }

    @Override
    public DriverInfoVo getDriverInfo(Long orderId, Long customerId) {
        OrderInfo orderInfo = orderInfoFeignClient.getOrderInfo(orderId).getData();
        if (orderInfo.getCustomerId().intValue() != customerId || orderInfo.getDriverId() == null) {
            throw new BusinessException(ResultCodeEnum.ILLEGAL_REQUEST);
        }

        Long driverId = orderInfo.getDriverId();
        Result<DriverInfoVo> driverInfo = driverInfoFeignClient.getDriverInfo(driverId);

        return driverInfo.getData();
    }

    @Override
    public OrderLocationVo getCacheOrderLocation(Long orderId, Long customerId) {
        //判断订单是否属于当前用户
        OrderInfo orderInfo = orderInfoFeignClient.getOrderInfo(orderId).getData();
        if (orderInfo.getCustomerId().intValue() != customerId || orderInfo.getDriverId() == null) {
            throw new BusinessException(ResultCodeEnum.ILLEGAL_REQUEST);
        }

        return locationFeignClient.getCacheOrderLocation(orderId).getData();
    }

    @Override
    public DrivingLineVo calculateDrivingLine(CalculateDrivingLineForm calculateDrivingLineForm) {
        //TODO 优化：路线在司机端和乘客端只计算一次
        return mapFeignClient.calculateDrivingLine(calculateDrivingLineForm).getData();
    }

    @Override
    public OrderServiceLastLocationVo getOrderServiceLastLocation(Long orderId) {
        return locationFeignClient.getOrderServiceLastLocation(orderId).getData();
    }

    @Override
    public PageVo<OrderListVo> findCustomerOrderPage(Long customerId, Long page, Long limit) {
        return orderInfoFeignClient.findCustomerOrderPage(customerId, page, limit).getData();
    }

    @GlobalTransactional
    @Override
    public WxPrepayVo createWxPayment(CreateWxPaymentForm createWxPaymentForm) {
        //1.获取订单支付相关信息
        OrderPayVo orderPayVo = orderInfoFeignClient.getOrderPayVo(createWxPaymentForm.getOrderNo(), createWxPaymentForm.getCustomerId()).getData();
        //判断是否在未支付状态
        if (orderPayVo.getStatus().intValue() != OrderStatus.UNPAID.getStatus().intValue()) {
            throw new BusinessException(ResultCodeEnum.ILLEGAL_REQUEST);
        }

        //获取司机和乘客openId
        String customerOpenId = customerInfoFeignClient.getCustomerOpenId(orderPayVo.getCustomerId()).getData();
        String driverOpenId = driverInfoFeignClient.getDriverOpenId(orderPayVo.getDriverId()).getData();

        //优惠券
        BigDecimal couponAmount = null;
        if (orderPayVo.getCouponAmount() == null && createWxPaymentForm.getCustomerCouponId() != null) {
            UseCouponForm useCouponForm = new UseCouponForm();
            useCouponForm.setCustomerCouponId(createWxPaymentForm.getCustomerCouponId());
            useCouponForm.setOrderId(orderPayVo.getOrderId());
            useCouponForm.setCustomerId(orderPayVo.getCustomerId());
            useCouponForm.setOrderAmount(orderPayVo.getPayAmount());
            couponAmount = couponFeignClient.useCoupon(useCouponForm).getData();
        }

        BigDecimal payAmount = orderPayVo.getPayAmount();
        if (couponAmount != null) {
            orderInfoFeignClient.updateCouponAmount(orderPayVo.getOrderId(), couponAmount);
            payAmount = payAmount.subtract(couponAmount);
        }

        PaymentInfoForm paymentInfoForm = new PaymentInfoForm();
        paymentInfoForm.setCustomerOpenId(customerOpenId);
        paymentInfoForm.setDriverOpenId(driverOpenId);
        paymentInfoForm.setOrderNo(orderPayVo.getOrderNo());
        paymentInfoForm.setAmount(payAmount);
        paymentInfoForm.setContent(orderPayVo.getContent());
        paymentInfoForm.setPayWay(1);

        return wxPayFeignClient.createWxPayment(paymentInfoForm).getData();

    }

    @Override
    public Boolean queryPayStatus(String orderNo) {
        return wxPayFeignClient.queryPayStatus(orderNo).getData();
    }

    @Override
    public Boolean cancelOrder(Long orderId, Long customerId) {
        Boolean success = orderInfoFeignClient.cancelOrder(orderId, customerId).getData();
        if (!success) {
            throw new BusinessException(ResultCodeEnum.DATA_ERROR);
        }
        locationFeignClient.removeOrderRelatedInfo(orderId);
        return true;
    }
}
