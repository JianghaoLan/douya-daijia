package org.lanjianghao.daijia.rules.service;

import org.lanjianghao.daijia.model.form.rules.RewardRuleRequestForm;
import org.lanjianghao.daijia.model.vo.rules.RewardRuleResponseVo;

public interface RewardRuleService {

    RewardRuleResponseVo calculateOrderRewardFee(RewardRuleRequestForm rewardRuleRequestForm);
}
