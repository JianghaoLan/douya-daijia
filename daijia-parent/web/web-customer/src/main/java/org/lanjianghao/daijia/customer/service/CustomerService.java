package org.lanjianghao.daijia.customer.service;

import org.lanjianghao.daijia.model.form.customer.UpdateWxPhoneForm;
import org.lanjianghao.daijia.model.vo.customer.CustomerLoginVo;

public interface CustomerService {


    String login(String code);

    CustomerLoginVo getCustomerLoginInfo(Long customerId);

    Boolean updateWxPhone(UpdateWxPhoneForm updateWxPhoneForm);
}
