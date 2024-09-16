package org.lanjianghao.daijia.customer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.lanjianghao.daijia.common.login.LoginRequired;
import org.lanjianghao.daijia.common.result.Result;
import org.lanjianghao.daijia.common.util.AuthContextHolder;
import org.lanjianghao.daijia.customer.service.OrderService;
import org.lanjianghao.daijia.model.form.customer.ExpectOrderForm;
import org.lanjianghao.daijia.model.form.customer.SubmitOrderForm;
import org.lanjianghao.daijia.model.form.map.CalculateDrivingLineForm;
import org.lanjianghao.daijia.model.form.payment.CreateWxPaymentForm;
import org.lanjianghao.daijia.model.vo.base.PageVo;
import org.lanjianghao.daijia.model.vo.customer.ExpectOrderVo;
import org.lanjianghao.daijia.model.vo.driver.DriverInfoVo;
import org.lanjianghao.daijia.model.vo.map.DrivingLineVo;
import org.lanjianghao.daijia.model.vo.map.OrderLocationVo;
import org.lanjianghao.daijia.model.vo.map.OrderServiceLastLocationVo;
import org.lanjianghao.daijia.model.vo.order.CurrentOrderInfoVo;
import org.lanjianghao.daijia.model.vo.order.OrderInfoVo;
import org.lanjianghao.daijia.model.vo.order.OrderListVo;
import org.lanjianghao.daijia.model.vo.payment.WxPrepayVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "订单API接口管理")
@RestController
@RequestMapping("/order")
@SuppressWarnings({"unchecked", "rawtypes"})
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Operation(summary = "查找乘客端当前订单")
    @LoginRequired
    @GetMapping("/searchCustomerCurrentOrder")
    public Result<CurrentOrderInfoVo> searchCustomerCurrentOrder() {
//        CurrentOrderInfoVo currentOrderInfoVo = new CurrentOrderInfoVo();
//        currentOrderInfoVo.setIsHasCurrentOrder(false);
        Long customerId = AuthContextHolder.getUserId();
        CurrentOrderInfoVo currentOrderInfoVo = orderService.searchCustomerCurrentOrder(customerId);
        return Result.ok(currentOrderInfoVo);
    }

    @Operation(summary = "预估订单数据")
    @LoginRequired
    @PostMapping("/expectOrder")
    public Result<ExpectOrderVo> expectOrder(@RequestBody ExpectOrderForm expectOrderForm) {
        return Result.ok(orderService.expectOrder(expectOrderForm));
    }

    @Operation(summary = "乘客下单")
    @LoginRequired
    @PostMapping("/submitOrder")
    public Result<Long> submitOrder(@RequestBody SubmitOrderForm submitOrderForm) {
        submitOrderForm.setCustomerId(AuthContextHolder.getUserId());
        return Result.ok(orderService.submitOrder(submitOrderForm));
    }

    @Operation(summary = "查询订单状态")
    @LoginRequired
    @GetMapping("/getOrderStatus/{orderId}")
    public Result<Integer> getOrderStatus(@PathVariable Long orderId) {
        return Result.ok(orderService.getOrderStatus(orderId));
    }

    @Operation(summary = "获取订单信息")
    @LoginRequired
    @GetMapping("/getOrderInfo/{orderId}")
    public Result<OrderInfoVo> getOrderInfo(@PathVariable Long orderId) {
        Long customerId = AuthContextHolder.getUserId();
        return Result.ok(orderService.getOrderInfo(orderId, customerId));
    }

    @Operation(summary = "根据订单id获取司机基本信息")
    @LoginRequired
    @GetMapping("/getDriverInfo/{orderId}")
    public Result<DriverInfoVo> getDriverInfo(@PathVariable Long orderId) {
        Long customerId = AuthContextHolder.getUserId();
        return Result.ok(orderService.getDriverInfo(orderId, customerId));
    }

    @Operation(summary = "司机赶往代驾起始点：获取订单经纬度位置")
    @LoginRequired
    @GetMapping("/getCacheOrderLocation/{orderId}")
    public Result<OrderLocationVo> getOrderLocation(@PathVariable Long orderId) {
        Long customerId = AuthContextHolder.getUserId();
        return Result.ok(orderService.getCacheOrderLocation(orderId, customerId));
    }

    @Operation(summary = "计算最佳驾驶线路")
    @LoginRequired
    @PostMapping("/calculateDrivingLine")
    public Result<DrivingLineVo> calculateDrivingLine(@RequestBody CalculateDrivingLineForm calculateDrivingLineForm) {
        return Result.ok(orderService.calculateDrivingLine(calculateDrivingLineForm));
    }

    @Operation(summary = "代驾服务：获取订单服务最后一个位置信息")
    @LoginRequired
    @GetMapping("/getOrderServiceLastLocation/{orderId}")
    public Result<OrderServiceLastLocationVo> getOrderServiceLastLocation(@PathVariable Long orderId) {
        return Result.ok(orderService.getOrderServiceLastLocation(orderId));
    }


    @Operation(summary = "获取乘客订单分页列表")
    @LoginRequired
    @GetMapping("findCustomerOrderPage/{page}/{limit}")
    public Result<PageVo<OrderListVo>> findCustomerOrderPage(
            @Parameter(name = "page", description = "当前页码", required = true)
            @PathVariable Long page,

            @Parameter(name = "limit", description = "每页记录数", required = true)
            @PathVariable Long limit) {
        Long customerId = AuthContextHolder.getUserId();
        PageVo<OrderListVo> pageVo = orderService.findCustomerOrderPage(customerId, page, limit);
        return Result.ok(pageVo);
    }

    @Operation(summary = "创建微信支付")
    @LoginRequired
    @PostMapping("/createWxPayment")
    public Result<WxPrepayVo> createWxPayment(@RequestBody CreateWxPaymentForm createWxPaymentForm) {
        Long customerId = AuthContextHolder.getUserId();
        createWxPaymentForm.setCustomerId(customerId);
        return Result.ok(orderService.createWxPayment(createWxPaymentForm));
    }

    @Operation(summary = "支付状态查询")
    @LoginRequired
    @GetMapping("/queryPayStatus/{orderNo}")
    public Result<Boolean> queryPayStatus(@PathVariable String orderNo) {
        return Result.ok(orderService.queryPayStatus(orderNo));
    }
}

