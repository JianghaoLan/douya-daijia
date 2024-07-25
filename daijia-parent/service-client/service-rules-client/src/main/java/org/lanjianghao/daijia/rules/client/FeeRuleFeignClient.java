package org.lanjianghao.daijia.rules.client;

import org.lanjianghao.daijia.common.result.Result;
import org.lanjianghao.daijia.model.form.rules.FeeRuleRequestForm;
import org.lanjianghao.daijia.model.vo.rules.FeeRuleResponseVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "service-rules")
public interface FeeRuleFeignClient {
    @PostMapping("/rules/fee/calculateOrderFee")
    Result<FeeRuleResponseVo> calculateOrderFee(@RequestBody FeeRuleRequestForm calculateOrderFeeForm);
}