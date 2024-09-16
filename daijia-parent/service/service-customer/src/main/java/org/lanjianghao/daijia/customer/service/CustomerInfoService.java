package org.lanjianghao.daijia.customer.service;

import org.lanjianghao.daijia.model.entity.customer.CustomerInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import org.lanjianghao.daijia.model.form.customer.UpdateWxPhoneForm;
import org.lanjianghao.daijia.model.vo.customer.CustomerLoginVo;

public interface CustomerInfoService extends IService<CustomerInfo> {

    Long login(String code);

    CustomerLoginVo getCustomerLoginInfo(Long customerId);

    Boolean updateWxPhoneNumber(UpdateWxPhoneForm updateWxPhoneForm);

    String getCustomerOpenId(Long customerId);
}
