package org.lanjianghao.daijia.dispatch.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.lanjianghao.daijia.common.result.Result;
import org.lanjianghao.daijia.dispatch.service.NewOrderService;
import org.lanjianghao.daijia.dispatch.xxl.client.XxlJobClient;
import org.lanjianghao.daijia.model.vo.dispatch.NewOrderTaskVo;
import org.lanjianghao.daijia.model.vo.order.NewOrderDataVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Tag(name = "司机新订单接口管理")
@RestController
@RequestMapping("/dispatch/newOrder")
@SuppressWarnings({"unchecked", "rawtypes"})
public class NewOrderController {

    @Autowired
    private NewOrderService newOrderService;

    @Operation(summary = "添加并开始新订单任务调度")
    @PostMapping("/addAndStartTask")
    public Result<Long> addAndStartTask(@RequestBody NewOrderTaskVo newOrderTaskVo) {
        return Result.ok(newOrderService.addAndStartTask(newOrderTaskVo));
    }

    @Operation(summary = "查询司机新订单数据")
    @GetMapping("/findNewOrderQueueData/{driverId}")
    public Result<List<NewOrderDataVo>> findNewOrderQueueData(@PathVariable Long driverId) {
        return Result.ok(newOrderService.findNewOrderQueueData(driverId));
    }

    @Operation(summary = "清空新订单队列数据")
    @GetMapping("/clearNewOrderQueueData/{driverId}")
    public Result<Boolean> clearNewOrderQueueData(@PathVariable Long driverId) {
        return Result.ok(newOrderService.clearNewOrderQueueData(driverId));
    }
}

