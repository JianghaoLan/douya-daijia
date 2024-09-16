package org.lanjianghao.daijia.rules.service;

import org.lanjianghao.daijia.model.form.rules.ProfitsharingRuleRequestForm;
import org.lanjianghao.daijia.model.vo.rules.ProfitsharingRuleResponseVo;

public interface ProfitsharingRuleService {

    ProfitsharingRuleResponseVo calculateOrderProfitsharingFee(ProfitsharingRuleRequestForm profitsharingRuleRequestForm);
}
