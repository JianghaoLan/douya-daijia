package org.lanjianghao.daijia.driver.service;

import org.lanjianghao.daijia.model.entity.driver.DriverAccount;
import com.baomidou.mybatisplus.extension.service.IService;
import org.lanjianghao.daijia.model.form.driver.TransferForm;

public interface DriverAccountService extends IService<DriverAccount> {


    Boolean transfer(TransferForm transferForm);
}
