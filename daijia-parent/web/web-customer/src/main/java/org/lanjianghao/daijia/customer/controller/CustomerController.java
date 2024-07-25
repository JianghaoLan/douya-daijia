package org.lanjianghao.daijia.customer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.lanjianghao.daijia.common.login.LoginRequired;
import org.lanjianghao.daijia.common.result.Result;
import org.lanjianghao.daijia.common.util.AuthContextHolder;
import org.lanjianghao.daijia.customer.service.CustomerService;
import org.lanjianghao.daijia.model.form.customer.UpdateWxPhoneForm;
import org.lanjianghao.daijia.model.vo.customer.CustomerLoginVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "客户API接口管理")
@RestController
@RequestMapping("/customer")
@SuppressWarnings({"unchecked", "rawtypes"})
public class CustomerController {
    @Autowired
    private CustomerService customerInfoService;

    @Operation(summary = "小程序授权登录")
    @GetMapping("/login/{code}")
    public Result<String> wxLogin(@PathVariable String code) {
        return Result.ok(customerInfoService.login(code));
    }

    @Operation(summary = "获取客户登录信息")
    @LoginRequired
    @GetMapping("/getCustomerLoginInfo")
    public Result<CustomerLoginVo> getCustomerLoginInfo() {
        Long userId = AuthContextHolder.getUserId();
        return Result.ok(customerInfoService.getCustomerLoginInfo(userId));
    }

    @Operation(summary = "更新用户微信手机号")
    @LoginRequired
    @PostMapping("/updateWxPhone")
    public Result<Boolean> updateWxPhone(@RequestBody UpdateWxPhoneForm updateWxPhoneForm) {
        updateWxPhoneForm.setCustomerId(AuthContextHolder.getUserId());
//        Boolean res = customerInfoService.updateWxPhone(updateWxPhoneForm);
        return Result.ok(true);
    }
}
