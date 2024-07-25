package org.lanjianghao.daijia.rules.service;

import org.lanjianghao.daijia.model.form.rules.FeeRuleRequestForm;
import org.lanjianghao.daijia.model.vo.rules.FeeRuleResponseVo;

public interface FeeRuleService {

    FeeRuleResponseVo calculateOrderFee(FeeRuleRequestForm calculateOrderFeeForm);
}
