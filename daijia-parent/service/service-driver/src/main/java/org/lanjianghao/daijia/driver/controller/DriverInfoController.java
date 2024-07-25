package org.lanjianghao.daijia.driver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.lanjianghao.daijia.common.result.Result;
import org.lanjianghao.daijia.driver.service.DriverInfoService;
import org.lanjianghao.daijia.model.form.driver.DriverFaceModelForm;
import org.lanjianghao.daijia.model.form.driver.UpdateDriverAuthInfoForm;
import org.lanjianghao.daijia.model.vo.driver.DriverAuthInfoVo;
import org.lanjianghao.daijia.model.vo.driver.DriverLoginVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "司机API接口管理")
@RestController
@RequestMapping(value="/driver/info")
@SuppressWarnings({"unchecked", "rawtypes"})
public class DriverInfoController {

    @Autowired
    private DriverInfoService driverInfoService;

    @Operation(summary = "小程序授权登录")
    @GetMapping("/login/{code}")
    public Result<Long> login(@PathVariable String code) {
        return Result.ok(driverInfoService.login(code));
    }

    @Operation(summary = "获取司机登录信息")
    @GetMapping("/getDriverLoginInfo/{driverId}")
    public Result<DriverLoginVo> getDriverLoginInfo(@PathVariable("driverId") Long driverId) {
        DriverLoginVo driverLoginVo = driverInfoService.getDriverLoginInfo(driverId);
        return Result.ok(driverLoginVo);
    }

    @Operation(summary = "获取司机的认证信息")
    @GetMapping("/getDriverAuthInfo/{driver}")
    public Result<DriverAuthInfoVo> getDriverAuthInfo(@PathVariable Long driver) {
        DriverAuthInfoVo info = driverInfoService.getDriverAuthInfo(driver);
        return Result.ok(info);
    }

    @Operation(summary = "更新司机认证信息")
    @PostMapping("/updateDriverAuthInfo")
    public Result<Boolean> updateDriverAuthInfo(@RequestBody UpdateDriverAuthInfoForm updateDriverAuthInfoForm) {
        boolean isSuccess = driverInfoService.updateDriverAuthInfo(updateDriverAuthInfoForm);
        return Result.ok(isSuccess);
    }

    @Operation(summary = "创建司机人脸模型")
    @PostMapping("/createDriverFaceModel")
    public Result<Boolean> createDriverFaceModel(@RequestBody DriverFaceModelForm faceModelForm) {
        boolean ret = driverInfoService.createDriverFaceModel(faceModelForm);
        return Result.ok(ret);
    }
}

