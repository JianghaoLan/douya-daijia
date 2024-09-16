package org.lanjianghao.daijia.rules.service.impl;

import org.lanjianghao.daijia.model.form.rules.ProfitsharingRuleRequest;
import org.lanjianghao.daijia.model.form.rules.ProfitsharingRuleRequestForm;
import org.lanjianghao.daijia.model.vo.rules.ProfitsharingRuleResponse;
import org.lanjianghao.daijia.model.vo.rules.ProfitsharingRuleResponseVo;
import org.lanjianghao.daijia.rules.mapper.ProfitsharingRuleMapper;
import org.lanjianghao.daijia.rules.service.ProfitsharingRuleService;
import lombok.extern.slf4j.Slf4j;
import org.lanjianghao.daijia.rules.utils.DroolsHelper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class ProfitsharingRuleServiceImpl implements ProfitsharingRuleService {

    @Autowired
    private ProfitsharingRuleMapper rewardRuleMapper;

    @Autowired
    private DroolsHelper droolsHelper;

    @Override
    public ProfitsharingRuleResponseVo calculateOrderProfitsharingFee(ProfitsharingRuleRequestForm profitsharingRuleRequestForm) {
        ProfitsharingRuleRequest request = new ProfitsharingRuleRequest();
        BeanUtils.copyProperties(profitsharingRuleRequestForm, request);

        ProfitsharingRuleResponse response = droolsHelper.calculateProfitsharingFee(request);

        ProfitsharingRuleResponseVo vo = new ProfitsharingRuleResponseVo();
        BeanUtils.copyProperties(response, vo);
        return vo;
    }
}
