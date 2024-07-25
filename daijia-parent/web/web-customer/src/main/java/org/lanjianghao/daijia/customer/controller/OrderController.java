package org.lanjianghao.daijia.customer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.lanjianghao.daijia.common.login.LoginRequired;
import org.lanjianghao.daijia.common.result.Result;
import org.lanjianghao.daijia.common.util.AuthContextHolder;
import org.lanjianghao.daijia.customer.service.OrderService;
import org.lanjianghao.daijia.model.form.customer.ExpectOrderForm;
import org.lanjianghao.daijia.model.form.customer.SubmitOrderForm;
import org.lanjianghao.daijia.model.vo.customer.ExpectOrderVo;
import org.lanjianghao.daijia.model.vo.order.CurrentOrderInfoVo;
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
        //TODO 完善真正的逻辑
        CurrentOrderInfoVo currentOrderInfoVo = new CurrentOrderInfoVo();
        currentOrderInfoVo.setIsHasCurrentOrder(false);
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
//    @LoginRequired
    @GetMapping("/getOrderStatus/{orderId}")
    public Result<Integer> getOrderStatus(@PathVariable Long orderId) {
        return Result.ok(orderService.getOrderStatus(orderId));
    }
}

