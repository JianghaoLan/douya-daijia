package org.lanjianghao.daijia.customer.service.impl;

import org.lanjianghao.daijia.common.execption.BusinessException;
import org.lanjianghao.daijia.common.result.Result;
import org.lanjianghao.daijia.common.result.ResultCodeEnum;
import org.lanjianghao.daijia.customer.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.lanjianghao.daijia.dispatch.client.NewOrderFeignClient;
import org.lanjianghao.daijia.driver.client.DriverInfoFeignClient;
import org.lanjianghao.daijia.map.client.LocationFeignClient;
import org.lanjianghao.daijia.map.client.MapFeignClient;
import org.lanjianghao.daijia.model.entity.order.OrderInfo;
import org.lanjianghao.daijia.model.form.customer.ExpectOrderForm;
import org.lanjianghao.daijia.model.form.customer.SubmitOrderForm;
import org.lanjianghao.daijia.model.form.map.CalculateDrivingLineForm;
import org.lanjianghao.daijia.model.form.order.OrderInfoForm;
import org.lanjianghao.daijia.model.form.rules.FeeRuleRequestForm;
import org.lanjianghao.daijia.model.vo.customer.ExpectOrderVo;
import org.lanjianghao.daijia.model.vo.dispatch.NewOrderTaskVo;
import org.lanjianghao.daijia.model.vo.driver.DriverInfoVo;
import org.lanjianghao.daijia.model.vo.map.DrivingLineVo;
import org.lanjianghao.daijia.model.vo.map.OrderLocationVo;
import org.lanjianghao.daijia.model.vo.map.OrderServiceLastLocationVo;
import org.lanjianghao.daijia.model.vo.order.CurrentOrderInfoVo;
import org.lanjianghao.daijia.model.vo.order.OrderInfoVo;
import org.lanjianghao.daijia.model.vo.rules.FeeRuleResponseVo;
import org.lanjianghao.daijia.order.client.OrderInfoFeignClient;
import org.lanjianghao.daijia.rules.client.FeeRuleFeignClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

        //远程调用定时查询附近可以接单的司机
        NewOrderTaskVo newOrderTaskVo = new NewOrderTaskVo();
        newOrderTaskVo.setOrderId(orderId);
        newOrderTaskVo.setStartLocation(orderInfoForm.getStartLocation());
        newOrderTaskVo.setStartPointLongitude(orderInfoForm.getStartPointLongitude());
        newOrderTaskVo.setStartPointLatitude(orderInfoForm.getStartPointLatitude());
        newOrderTaskVo.setEndLocation(orderInfoForm.getEndLocation());
        newOrderTaskVo.setEndPointLongitude(orderInfoForm.getEndPointLongitude());
        newOrderTaskVo.setEndPointLatitude(orderInfoForm.getEndPointLatitude());
        newOrderTaskVo.setExpectAmount(orderInfoForm.getExpectAmount());
        newOrderTaskVo.setExpectDistance(orderInfoForm.getExpectDistance());
        newOrderTaskVo.setExpectTime(drivingLineVo.getDuration());
        newOrderTaskVo.setFavourFee(orderInfoForm.getFavourFee());
        Long jobId = newOrderFeignClient.addAndStartTask(newOrderTaskVo).getData();

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
        OrderInfoVo vo = new OrderInfoVo();
        BeanUtils.copyProperties(info, vo);
        vo.setOrderId(info.getId());
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
}
