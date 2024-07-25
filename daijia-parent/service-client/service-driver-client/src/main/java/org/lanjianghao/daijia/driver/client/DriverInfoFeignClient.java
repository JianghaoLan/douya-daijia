package org.lanjianghao.daijia.driver.client;

import org.lanjianghao.daijia.common.result.Result;
import org.lanjianghao.daijia.model.form.driver.DriverFaceModelForm;
import org.lanjianghao.daijia.model.form.driver.UpdateDriverAuthInfoForm;
import org.lanjianghao.daijia.model.vo.driver.DriverAuthInfoVo;
import org.lanjianghao.daijia.model.vo.driver.DriverLoginVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "service-driver")
public interface DriverInfoFeignClient {
    @GetMapping("/driver/info/login/{code}")
    Result<Long> login(@PathVariable String code);

    @GetMapping("/driver/info/getDriverLoginInfo/{driverId}")
    Result<DriverLoginVo> getDriverLoginInfo(@PathVariable("driverId") Long driverId);

    @GetMapping("/driver/info/getDriverAuthInfo/{driver}")
    Result<DriverAuthInfoVo> getDriverAuthInfo(@PathVariable Long driver);

    @PostMapping("/driver/info/updateDriverAuthInfo")
    Result<Boolean> updateDriverAuthInfo(@RequestBody UpdateDriverAuthInfoForm updateDriverAuthInfoForm);

    @PostMapping("/driver/info/createDriverFaceModel")
    Result<Boolean> createDriverFaceModel(@RequestBody DriverFaceModelForm faceModelForm);
}
