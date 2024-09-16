package org.lanjianghao.daijia.rules.service.impl;

import org.joda.time.DateTime;
import org.kie.api.command.Command;
import org.kie.api.runtime.ExecutionResults;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.internal.command.CommandFactory;
import org.lanjianghao.daijia.model.form.rules.FeeRuleRequest;
import org.lanjianghao.daijia.model.form.rules.FeeRuleRequestForm;
import org.lanjianghao.daijia.model.vo.rules.FeeRuleResponse;
import org.lanjianghao.daijia.model.vo.rules.FeeRuleResponseVo;
import org.lanjianghao.daijia.rules.service.FeeRuleService;
import lombok.extern.slf4j.Slf4j;
import org.lanjianghao.daijia.rules.utils.DroolsHelper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class FeeRuleServiceImpl implements FeeRuleService {

    @Autowired
    private KieContainer kieContainer;

    @Autowired
    private DroolsHelper droolsHelper;

    @Override
    public FeeRuleResponseVo calculateOrderFee(FeeRuleRequestForm calculateOrderFeeForm) {
        FeeRuleRequest feeRuleRequest = new FeeRuleRequest();
        feeRuleRequest.setDistance(calculateOrderFeeForm.getDistance());
        feeRuleRequest.setStartTime(new DateTime(calculateOrderFeeForm.getStartTime()).toString("HH:mm:ss"));
        feeRuleRequest.setWaitMinute(calculateOrderFeeForm.getWaitMinute());
        BeanUtils.copyProperties(calculateOrderFeeForm, feeRuleRequest);

        //调用drools计算
        FeeRuleResponse feeRuleResponse = droolsHelper.calculateOrderFee(feeRuleRequest);

        FeeRuleResponseVo res = new FeeRuleResponseVo();
        BeanUtils.copyProperties(feeRuleResponse, res);

        return res;
    }
}
