package org.lanjianghao.daijia.rules.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.lanjianghao.daijia.common.result.Result;
import org.lanjianghao.daijia.model.form.rules.ProfitsharingRuleRequestForm;
import org.lanjianghao.daijia.model.vo.rules.ProfitsharingRuleResponseVo;
import org.lanjianghao.daijia.rules.service.ProfitsharingRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/rules/profitsharing")
@SuppressWarnings({"unchecked", "rawtypes"})
public class ProfitsharingRuleController {

    @Autowired
    private ProfitsharingRuleService profitsharingRuleService;

    @Operation(summary = "计算系统分账费用")
    @PostMapping("/calculateOrderProfitsharingFee")
    public Result<ProfitsharingRuleResponseVo> calculateOrderProfitsharingFee(@RequestBody ProfitsharingRuleRequestForm profitsharingRuleRequestForm) {
        return Result.ok(profitsharingRuleService.calculateOrderProfitsharingFee(profitsharingRuleRequestForm));
    }

}

