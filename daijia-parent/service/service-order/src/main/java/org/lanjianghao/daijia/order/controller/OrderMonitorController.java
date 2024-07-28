package org.lanjianghao.daijia.order.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.lanjianghao.daijia.common.result.Result;
import org.lanjianghao.daijia.model.entity.order.OrderMonitorRecord;
import org.lanjianghao.daijia.order.service.OrderMonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order/monitor")
@SuppressWarnings({"unchecked", "rawtypes"})
public class OrderMonitorController {

    @Autowired
    private OrderMonitorService orderMonitorService;

    @Operation(summary = "保存订单监控记录数据")
    @PostMapping("/saveOrderMonitorRecord")
    public Result<Boolean> saveMonitorRecord(@RequestBody OrderMonitorRecord orderMonitorRecord) {
        return Result.ok(orderMonitorService.saveOrderMonitorRecord(orderMonitorRecord));
    }

}

