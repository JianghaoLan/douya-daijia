package org.lanjianghao.daijia.rules.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.lanjianghao.daijia.common.result.Result;
import org.lanjianghao.daijia.model.form.rules.FeeRuleRequestForm;
import org.lanjianghao.daijia.model.vo.rules.FeeRuleResponseVo;
import org.lanjianghao.daijia.rules.service.FeeRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/rules/fee")
@SuppressWarnings({"unchecked", "rawtypes"})
public class FeeRuleController {

    @Autowired
    private FeeRuleService feeRuleService;

    @Operation(summary = "计算订单费用")
    @PostMapping("/calculateOrderFee")
    public Result<FeeRuleResponseVo> calculateOrderFee(@RequestBody FeeRuleRequestForm calculateOrderFeeForm) {
        FeeRuleResponseVo res = feeRuleService.calculateOrderFee(calculateOrderFeeForm);
        return Result.ok(res);
    }

}

