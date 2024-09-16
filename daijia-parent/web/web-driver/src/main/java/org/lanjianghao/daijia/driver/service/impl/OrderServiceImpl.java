package org.lanjianghao.daijia.driver.service.impl;

import com.alibaba.fastjson2.JSON;
import lombok.SneakyThrows;
import org.joda.time.DateTime;
import org.lanjianghao.daijia.common.constant.SystemConstant;
import org.lanjianghao.daijia.common.execption.BusinessException;
import org.lanjianghao.daijia.common.result.Result;
import org.lanjianghao.daijia.common.result.ResultCodeEnum;
import org.lanjianghao.daijia.common.util.LocationUtil;
import org.lanjianghao.daijia.dispatch.client.NewOrderFeignClient;
import org.lanjianghao.daijia.driver.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.lanjianghao.daijia.map.client.LocationFeignClient;
import org.lanjianghao.daijia.map.client.MapFeignClient;
import org.lanjianghao.daijia.model.entity.order.OrderInfo;
import org.lanjianghao.daijia.model.enums.OrderStatus;
import org.lanjianghao.daijia.model.form.map.CalculateDrivingLineForm;
import org.lanjianghao.daijia.model.form.order.OrderFeeForm;
import org.lanjianghao.daijia.model.form.order.StartDriveForm;
import org.lanjianghao.daijia.model.form.order.UpdateOrderBillForm;
import org.lanjianghao.daijia.model.form.order.UpdateOrderCartForm;
import org.lanjianghao.daijia.model.form.rules.FeeRuleRequestForm;
import org.lanjianghao.daijia.model.form.rules.ProfitsharingRuleRequestForm;
import org.lanjianghao.daijia.model.form.rules.RewardRuleRequestForm;
import org.lanjianghao.daijia.model.vo.base.PageVo;
import org.lanjianghao.daijia.model.vo.map.DrivingLineVo;
import org.lanjianghao.daijia.model.vo.map.OrderLocationVo;
import org.lanjianghao.daijia.model.vo.map.OrderServiceLastLocationVo;
import org.lanjianghao.daijia.model.vo.order.*;
import org.lanjianghao.daijia.model.vo.rules.FeeRuleResponseVo;
import org.lanjianghao.daijia.model.vo.rules.ProfitsharingRuleResponseVo;
import org.lanjianghao.daijia.model.vo.rules.RewardRuleResponseVo;
import org.lanjianghao.daijia.order.client.OrderInfoFeignClient;
import org.lanjianghao.daijia.rules.client.FeeRuleFeignClient;
import org.lanjianghao.daijia.rules.client.ProfitsharingRuleFeignClient;
import org.lanjianghao.daijia.rules.client.RewardRuleFeignClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderInfoFeignClient orderInfoFeignClient;

    @Autowired
    private NewOrderFeignClient newOrderFeignClient;

    @Autowired
    private MapFeignClient mapFeignClient;

    @Autowired
    private LocationFeignClient locationFeignClient;

    @Autowired
    private FeeRuleFeignClient feeRuleFeignClient;

    @Autowired
    private RewardRuleFeignClient rewardRuleFeignClient;

    @Autowired
    private ProfitsharingRuleFeignClient profitsharingRuleFeignClient;

    @Override
    public Integer getOrderStatus(Long orderId) {
        return orderInfoFeignClient.getOrderStatus(orderId).getData();
    }

    @Override
    public List<NewOrderDataVo> findNewOrderQueueData(Long driverId) {
        return newOrderFeignClient.findNewOrderQueueData(driverId).getData();
    }

    @Override
    public Boolean robNewOrder(Long driverId, Long orderId) {
        return orderInfoFeignClient.robNewOrder(driverId, orderId).getData();
    }

    @Override
    public CurrentOrderInfoVo searchDriverCurrentOrder(Long driverId) {
        return orderInfoFeignClient.searchDriverCurrentOrder(driverId).getData();
    }

    @Override
    public OrderInfoVo getOrderInfo(Long orderId, Long driverId) {
        OrderInfo info = orderInfoFeignClient.getOrderInfo(orderId).getData();
        if (info.getDriverId().intValue() != driverId) {
            throw new BusinessException(ResultCodeEnum.DATA_ERROR);
        }

        //获取账单和分账数据
        OrderBillVo orderBillInfo = null;
        OrderProfitsharingVo orderProfitSharing = null;
        if (info.getStatus() >= OrderStatus.END_SERVICE.getStatus()) {
            orderBillInfo = orderInfoFeignClient.getOrderBillInfo(orderId).getData();
            orderProfitSharing = orderInfoFeignClient.getOrderProfitsharing(orderId).getData();
        }

        OrderInfoVo vo = new OrderInfoVo();
        BeanUtils.copyProperties(info, vo);
        vo.setOrderId(info.getId());
        vo.setOrderBillVo(orderBillInfo);
        vo.setOrderProfitsharingVo(orderProfitSharing);
        return vo;
    }

    @Override
    public DrivingLineVo calculateDrivingLine(CalculateDrivingLineForm calculateDrivingLineForm) {
        return mapFeignClient.calculateDrivingLine(calculateDrivingLineForm).getData();
    }

    @Override
    public Boolean driverArriveStartLocation(Long orderId, Long driverId) {
        OrderInfo orderInfo = orderInfoFeignClient.getOrderInfo(orderId).getData();

        OrderLocationVo cachedLocation = locationFeignClient.getCacheOrderLocation(orderId).getData();
        double distance = LocationUtil.getDistance(
                orderInfo.getStartPointLatitude().doubleValue(), orderInfo.getStartPointLongitude().doubleValue(),
                cachedLocation.getLatitude().doubleValue(), cachedLocation.getLongitude().doubleValue());
        if (distance > SystemConstant.DRIVER_START_LOCATION_DISTION) {
            throw new BusinessException(ResultCodeEnum.DRIVER_START_LOCATION_DISTION_ERROR);
        }

        return orderInfoFeignClient.driverArriveStartLocation(orderId, driverId).getData();
    }

    @Override
    public Boolean updateOrderCart(UpdateOrderCartForm updateOrderCartForm) {
        return orderInfoFeignClient.updateOrderCart(updateOrderCartForm).getData();
    }

    @Override
    public Boolean startDrive(StartDriveForm startDriveForm) {
        return orderInfoFeignClient.startDrive(startDriveForm).getData();
    }

//    @Override
//    public Boolean endDrive(OrderFeeForm form) {
//        // 根据订单id获取订单信息，判断当前订单是否属于当前司机
//        OrderInfo orderInfo = orderInfoFeignClient.getOrderInfo(form.getOrderId()).getData();
//        if (orderInfo.getDriverId().longValue() != form.getDriverId()) {
//            throw new BusinessException(ResultCodeEnum.ILLEGAL_REQUEST);
//        }
//
//        //计算现在位置是否距离终点过远，防止刷单
//        OrderServiceLastLocationVo lastLocation =
//                locationFeignClient.getOrderServiceLastLocation(form.getOrderId()).getData();
//        double distance = LocationUtil.getDistance(
//                orderInfo.getEndPointLatitude().doubleValue(), orderInfo.getEndPointLongitude().doubleValue(),
//                lastLocation.getLatitude().doubleValue(), lastLocation.getLongitude().doubleValue());
//        if (distance > SystemConstant.DRIVER_END_LOCATION_DISTION) {
//            throw new BusinessException(ResultCodeEnum.DRIVER_END_LOCATION_DISTION_ERROR);
//        }
//
//        //计算订单实际里程
//        BigDecimal realDistance = locationFeignClient.calculateOrderRealDistance(form.getOrderId()).getData();
//
//        //计算代驾实际费用
//        FeeRuleRequestForm feeRuleRequestForm = new FeeRuleRequestForm();
//        Integer waitMinute = (int)((orderInfo.getStartServiceTime().getTime() - orderInfo.getArriveTime().getTime()) / 60000);
//        feeRuleRequestForm.setWaitMinute(waitMinute);
//        feeRuleRequestForm.setDistance(realDistance);
//        feeRuleRequestForm.setStartTime(orderInfo.getStartServiceTime());
//        FeeRuleResponseVo feeRuleResponseVo = feeRuleFeignClient.calculateOrderFee(feeRuleRequestForm).getData();
//        BigDecimal totalAmount = feeRuleResponseVo.getTotalAmount()
//                .add(form.getTollFee())
//                .add(form.getParkingFee())
//                .add(form.getOtherFee())
//                .add(orderInfo.getFavourFee());
//        feeRuleResponseVo.setTotalAmount(totalAmount);
//
//        //计算系统奖励
//        String startTime = new DateTime(orderInfo.getStartServiceTime()).toString("yyyy-MM-dd") + " 00:00:00";
//        String endTime = new DateTime(orderInfo.getStartServiceTime()).plusDays(1).toString("yyyy-MM-dd") + " 00:00:00";
//        Long orderNum = orderInfoFeignClient.getOrderNumByTime(startTime, endTime).getData();
//        RewardRuleRequestForm rewardRuleRequestForm = new RewardRuleRequestForm();
//        rewardRuleRequestForm.setOrderNum(orderNum);
//        rewardRuleRequestForm.setStartTime(orderInfo.getStartServiceTime());
//        RewardRuleResponseVo rewardRuleResponse = rewardRuleFeignClient.calculateOrderRewardFee(rewardRuleRequestForm).getData();
//
//        //计算分账信息
//        ProfitsharingRuleRequestForm profitsharingRuleRequestForm = new ProfitsharingRuleRequestForm();
//        profitsharingRuleRequestForm.setOrderNum(orderNum);
//        profitsharingRuleRequestForm.setOrderAmount(totalAmount);
//        ProfitsharingRuleResponseVo profitsharingRuleResponseVo =
//                profitsharingRuleFeignClient.calculateOrderProfitsharingFee(profitsharingRuleRequestForm).getData();
//
//        //封装实体类，结束代驾，添加账单和分账信息
//        UpdateOrderBillForm updateOrderBillForm = new UpdateOrderBillForm();
//        updateOrderBillForm.setOrderId(form.getOrderId());
//        updateOrderBillForm.setDriverId(form.getDriverId());
//        //路桥费、停车费、其他费用
//        updateOrderBillForm.setTollFee(form.getTollFee());
//        updateOrderBillForm.setParkingFee(form.getParkingFee());
//        updateOrderBillForm.setOtherFee(form.getOtherFee());
//        //乘客好处费
//        updateOrderBillForm.setFavourFee(orderInfo.getFavourFee());
//        //实际里程
//        updateOrderBillForm.setRealDistance(realDistance);
//        //订单奖励信息
//        BeanUtils.copyProperties(rewardRuleResponse, updateOrderBillForm);
//        //代驾费用信息
//        BeanUtils.copyProperties(feeRuleResponseVo, updateOrderBillForm);
//        //分账相关信息
//        BeanUtils.copyProperties(profitsharingRuleResponseVo, updateOrderBillForm);
//        updateOrderBillForm.setProfitsharingRuleId(profitsharingRuleResponseVo.getProfitsharingRuleId());
//        updateOrderBillForm.setProfitsharingRuleId(0L);         //TODO 规则未保存数据库，无ID，0占位
//        log.info("结束代驾，更新账单信息：{}", JSON.toJSONString(updateOrderBillForm));
//
//        return orderInfoFeignClient.endDrive(updateOrderBillForm).getData();
//    }

    //多线程优化
    @SneakyThrows
    @Override
    public Boolean endDrive(OrderFeeForm form) {
        CompletableFuture<OrderInfo> getOrderInfoFuture = CompletableFuture.supplyAsync(
                () -> {
                    // 根据订单id获取订单信息，判断当前订单是否属于当前司机
                    OrderInfo orderInfo = orderInfoFeignClient.getOrderInfo(form.getOrderId()).getData();
                    if (orderInfo.getDriverId().longValue() != form.getDriverId()) {
                        throw new BusinessException(ResultCodeEnum.ILLEGAL_REQUEST);
                    }
                    return orderInfo;
                }
        );

        CompletableFuture<OrderServiceLastLocationVo> getOrderServiceLastLocationVoFuture = CompletableFuture.supplyAsync(
                () -> {
                    //得到当前位置
                    return locationFeignClient.getOrderServiceLastLocation(form.getOrderId()).getData();
                }
        );


        CompletableFuture.allOf(getOrderInfoFuture, getOrderServiceLastLocationVoFuture).join();

        OrderInfo orderInfo = getOrderInfoFuture.get();
        OrderServiceLastLocationVo lastLocation = getOrderServiceLastLocationVoFuture.get();

        //计算当前位置是否距离终点过远，防止刷单
        double distance = LocationUtil.getDistance(
                orderInfo.getEndPointLatitude().doubleValue(), orderInfo.getEndPointLongitude().doubleValue(),
                lastLocation.getLatitude().doubleValue(), lastLocation.getLongitude().doubleValue());
        if (distance > SystemConstant.DRIVER_END_LOCATION_DISTION) {
            throw new BusinessException(ResultCodeEnum.DRIVER_END_LOCATION_DISTION_ERROR);
        }

        CompletableFuture<BigDecimal> calcRealDistanceFuture = CompletableFuture.supplyAsync(
                () -> {
                    //计算订单实际里程
                    return locationFeignClient.calculateOrderRealDistance(form.getOrderId()).getData();
                }
        );

        CompletableFuture<FeeRuleResponseVo> calcFeeFuture = calcRealDistanceFuture.thenApplyAsync(
                (realDistance) -> {
                    //计算代驾实际费用
                    FeeRuleRequestForm feeRuleRequestForm = new FeeRuleRequestForm();
                    Integer waitMinute = (int) ((orderInfo.getStartServiceTime().getTime() - orderInfo.getArriveTime().getTime()) / 60000);
                    feeRuleRequestForm.setWaitMinute(waitMinute);
                    feeRuleRequestForm.setDistance(realDistance);
                    feeRuleRequestForm.setStartTime(orderInfo.getStartServiceTime());
                    FeeRuleResponseVo feeRuleResponseVo = feeRuleFeignClient.calculateOrderFee(feeRuleRequestForm).getData();
                    BigDecimal totalAmount = feeRuleResponseVo.getTotalAmount()
                            .add(form.getTollFee())
                            .add(form.getParkingFee())
                            .add(form.getOtherFee())
                            .add(orderInfo.getFavourFee());
                    feeRuleResponseVo.setTotalAmount(totalAmount);
                    return feeRuleResponseVo;
                }
        );

        CompletableFuture<Long> getOrderNumFuture = CompletableFuture.supplyAsync(
                () -> {
                    //计算系统奖励
                    String startTime = new DateTime(orderInfo.getStartServiceTime()).toString("yyyy-MM-dd") + " 00:00:00";
                    String endTime = new DateTime(orderInfo.getStartServiceTime()).plusDays(1).toString("yyyy-MM-dd") + " 00:00:00";
                    return orderInfoFeignClient.getOrderNumByTime(startTime, endTime).getData();
                }
        );

        CompletableFuture<RewardRuleResponseVo> calcRewardFeeFuture = getOrderNumFuture.thenApplyAsync(
                (orderNum) -> {
                    RewardRuleRequestForm rewardRuleRequestForm = new RewardRuleRequestForm();
                    rewardRuleRequestForm.setOrderNum(orderNum);
                    rewardRuleRequestForm.setStartTime(orderInfo.getStartServiceTime());
                    return rewardRuleFeignClient.calculateOrderRewardFee(rewardRuleRequestForm).getData();
                }
        );

//        CompletableFuture.allOf(getOrderNumFuture, calcFeeFuture).join();
//        FeeRuleResponseVo feeRuleResponseVo = calcFeeFuture.get();
//        Long orderNum = getOrderNumFuture.get();

        CompletableFuture<ProfitsharingRuleResponseVo> calcProfitSharingFuture = calcFeeFuture.thenCombineAsync(getOrderNumFuture,
                (feeRuleResponseVo, orderNum) -> {
                    //计算分账信息
                    ProfitsharingRuleRequestForm profitsharingRuleRequestForm = new ProfitsharingRuleRequestForm();
                    profitsharingRuleRequestForm.setOrderNum(orderNum);
                    profitsharingRuleRequestForm.setOrderAmount(feeRuleResponseVo.getTotalAmount());
                    return profitsharingRuleFeignClient.calculateOrderProfitsharingFee(profitsharingRuleRequestForm).getData();
                }
        );

        //得到所有异步结果
        BigDecimal realDistance = calcRealDistanceFuture.get();
        FeeRuleResponseVo feeRuleResponseVo = calcFeeFuture.get();
        RewardRuleResponseVo rewardRuleResponse = calcRewardFeeFuture.get();
        ProfitsharingRuleResponseVo profitsharingRuleResponseVo = calcProfitSharingFuture.get();

        //封装实体类，结束代驾，添加账单和分账信息
        UpdateOrderBillForm updateOrderBillForm = new UpdateOrderBillForm();
        updateOrderBillForm.setOrderId(form.getOrderId());
        updateOrderBillForm.setDriverId(form.getDriverId());
        //路桥费、停车费、其他费用
        updateOrderBillForm.setTollFee(form.getTollFee());
        updateOrderBillForm.setParkingFee(form.getParkingFee());
        updateOrderBillForm.setOtherFee(form.getOtherFee());
        //乘客好处费
        updateOrderBillForm.setFavourFee(orderInfo.getFavourFee());
        //实际里程
        updateOrderBillForm.setRealDistance(realDistance);
        //订单奖励信息
        BeanUtils.copyProperties(rewardRuleResponse, updateOrderBillForm);
        //代驾费用信息
        BeanUtils.copyProperties(feeRuleResponseVo, updateOrderBillForm);
        //分账相关信息
        BeanUtils.copyProperties(profitsharingRuleResponseVo, updateOrderBillForm);
        updateOrderBillForm.setProfitsharingRuleId(profitsharingRuleResponseVo.getProfitsharingRuleId());
        updateOrderBillForm.setProfitsharingRuleId(0L);         //TODO 规则未保存数据库，无ID，0占位
        log.info("结束代驾，更新账单信息：{}", JSON.toJSONString(updateOrderBillForm));

        return orderInfoFeignClient.endDrive(updateOrderBillForm).getData();
    }


    @Override
    public PageVo<OrderListVo> findDriverOrderPage(Long driverId, Long page, Long limit) {
        return orderInfoFeignClient.findDriverOrderPage(driverId, page, limit).getData();
    }

    @Override
    public Boolean sendOrderBillInfo(Long orderId, Long driverId) {
        return orderInfoFeignClient.sendOrderBillInfo(orderId, driverId).getData();
    }
}
