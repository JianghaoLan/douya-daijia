package org.lanjianghao.daijia.driver.service;

import org.lanjianghao.daijia.model.form.driver.DriverFaceModelForm;
import org.lanjianghao.daijia.model.form.driver.UpdateDriverAuthInfoForm;
import org.lanjianghao.daijia.model.vo.driver.DriverAuthInfoVo;
import org.lanjianghao.daijia.model.vo.driver.DriverLoginVo;

public interface DriverService {


    String login(String code);

    DriverLoginVo getDriverLoginInfo(Long driverId);

    DriverAuthInfoVo getDriverAuthInfo(Long driverId);

    boolean updateDriverAuthInfo(UpdateDriverAuthInfoForm updateDriverAuthInfoForm);

    Boolean creatDriverFaceModel(DriverFaceModelForm driverFaceModelForm);
}
