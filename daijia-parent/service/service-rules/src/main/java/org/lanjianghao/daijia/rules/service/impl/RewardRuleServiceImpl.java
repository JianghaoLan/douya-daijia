package org.lanjianghao.daijia.rules.service.impl;

import org.lanjianghao.daijia.model.form.rules.RewardRuleRequest;
import org.lanjianghao.daijia.model.form.rules.RewardRuleRequestForm;
import org.lanjianghao.daijia.model.vo.rules.RewardRuleResponse;
import org.lanjianghao.daijia.model.vo.rules.RewardRuleResponseVo;
import org.lanjianghao.daijia.rules.service.RewardRuleService;
import lombok.extern.slf4j.Slf4j;
import org.lanjianghao.daijia.rules.utils.DroolsHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class RewardRuleServiceImpl implements RewardRuleService {

    @Autowired
    DroolsHelper droolsHelper;

    @Override
    public RewardRuleResponseVo calculateOrderRewardFee(RewardRuleRequestForm rewardRuleRequestForm) {
        RewardRuleRequest rewardRuleRequest = new RewardRuleRequest();
        rewardRuleRequest.setOrderNum(rewardRuleRequestForm.getOrderNum());
//        rewardRuleRequest.setStartTime(new Date(rewardRuleRequestForm.getStartTime()));

        RewardRuleResponse response = droolsHelper.calculateReward(rewardRuleRequest);

        RewardRuleResponseVo vo = new RewardRuleResponseVo();
        vo.setRewardAmount(response.getRewardAmount());
        return vo;
    }
}
