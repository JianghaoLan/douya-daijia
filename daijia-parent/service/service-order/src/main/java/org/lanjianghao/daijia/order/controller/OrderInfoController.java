package org.lanjianghao.daijia.order.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.lanjianghao.daijia.common.result.Result;
import org.lanjianghao.daijia.model.form.order.OrderInfoForm;
import org.lanjianghao.daijia.order.service.OrderInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@Tag(name = "订单API接口管理")
@RestController
@RequestMapping(value="/order/info")
@SuppressWarnings({"unchecked", "rawtypes"})
public class OrderInfoController {

    @Autowired
    private OrderInfoService orderInfoService;

    @Operation(summary = "保存订单信息")
    @PostMapping("/saveOrderInfo")
    public Result<Long> saveOrderInfo(@RequestBody OrderInfoForm orderInfoForm) {
        return Result.ok(orderInfoService.saveOrderInfo(orderInfoForm));
    }

    @Operation(summary = "根据订单id获取订单状态")
    @GetMapping("/getOrderStatus/{orderId}")
    public Result<Integer> getOrderStatus(@PathVariable Long orderId) {
        return Result.ok(orderInfoService.getOrderStatus(orderId));
    }
}

