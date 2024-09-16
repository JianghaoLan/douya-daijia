package org.lanjianghao.daijia.rules.client;

import org.lanjianghao.daijia.common.result.Result;
import org.lanjianghao.daijia.model.form.rules.RewardRuleRequestForm;
import org.lanjianghao.daijia.model.vo.rules.RewardRuleResponseVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "service-rules")
public interface RewardRuleFeignClient {

    @PostMapping("/rules/reward/calculateOrderRewardFee")
    Result<RewardRuleResponseVo> calculateOrderRewardFee(@RequestBody RewardRuleRequestForm rewardRuleRequestForm);

}