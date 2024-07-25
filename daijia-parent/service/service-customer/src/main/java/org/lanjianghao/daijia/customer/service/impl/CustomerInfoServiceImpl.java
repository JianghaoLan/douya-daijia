package org.lanjianghao.daijia.customer.service.impl;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import me.chanjar.weixin.common.error.WxErrorException;
import org.lanjianghao.daijia.customer.mapper.CustomerInfoMapper;
import org.lanjianghao.daijia.customer.mapper.CustomerLoginLogMapper;
import org.lanjianghao.daijia.customer.service.CustomerInfoService;
import org.lanjianghao.daijia.model.entity.customer.CustomerInfo;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.lanjianghao.daijia.model.entity.customer.CustomerLoginLog;
import org.lanjianghao.daijia.model.form.customer.UpdateWxPhoneForm;
import org.lanjianghao.daijia.model.vo.customer.CustomerLoginVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class CustomerInfoServiceImpl extends ServiceImpl<CustomerInfoMapper, CustomerInfo> implements CustomerInfoService {

    @Autowired
    private WxMaService wxMaService;

    @Autowired
    private CustomerLoginLogMapper customerLoginLogMapper;

    private CustomerInfo newCustomer(String openId) {
        //首次登录
        CustomerInfo customerInfo = new CustomerInfo();
        customerInfo.setWxOpenId(openId);
        customerInfo.setNickname(String.valueOf(System.currentTimeMillis()));
        customerInfo.setAvatarUrl("https://oss.aliyuncs.com/aliyun_id_photo_bucket/default_handsome.jpg");
        this.save(customerInfo);
        return customerInfo;
    }

    private void logCustomerLogin(CustomerInfo customerInfo) {
        CustomerLoginLog customerLoginLog = new CustomerLoginLog();
        customerLoginLog.setCustomerId(customerInfo.getId());
        customerLoginLog.setMsg("小程序登录");
        customerLoginLogMapper.insert(customerLoginLog);
    }

    @Override
    public Long login(String code) {
        String openId;
        try {
            WxMaJscode2SessionResult sessionInfo = wxMaService.getUserService().getSessionInfo(code);
            openId = sessionInfo.getOpenid();
            log.info("[小程序授权] openId={}", openId);
        } catch (WxErrorException e) {
            throw new RuntimeException(e);
        }

        //根据openId查询数据库，判断是否首次登录
        CustomerInfo customerInfo = this.getOne(
                new LambdaQueryWrapper<CustomerInfo>().eq(CustomerInfo::getWxOpenId, openId));

        //非首次登录
        if (customerInfo == null) {
            customerInfo = newCustomer(openId);
        }

        //记录登录日志
        logCustomerLogin(customerInfo);

        return customerInfo.getId();
    }

    @Override
    public CustomerLoginVo getCustomerLoginInfo(Long customerId) {
        CustomerInfo customer = this.getById(customerId);
        CustomerLoginVo loginInfo = new CustomerLoginVo();
        BeanUtils.copyProperties(customer, loginInfo);
        loginInfo.setIsBindPhone(StringUtils.hasText(customer.getPhone()));
        return loginInfo;
    }

    @Override
    public Boolean updateWxPhoneNumber(UpdateWxPhoneForm updateWxPhoneForm) {
        WxMaPhoneNumberInfo phoneNoInfo;
        try {
            phoneNoInfo = wxMaService.getUserService().getPhoneNoInfo(updateWxPhoneForm.getCode());
        } catch (WxErrorException e) {
            throw new RuntimeException(e);
        }
        String phoneNumber = phoneNoInfo.getPhoneNumber();
        CustomerInfo forUpdate = new CustomerInfo();
        forUpdate.setPhone(phoneNumber);
        forUpdate.setId(updateWxPhoneForm.getCustomerId());
        return this.updateById(forUpdate);
    }

}
