package org.lanjianghao.daijia.customer.service.impl;

import org.lanjianghao.daijia.common.constant.RedisConstant;
import org.lanjianghao.daijia.common.execption.BusinessException;
import org.lanjianghao.daijia.common.result.Result;
import org.lanjianghao.daijia.common.result.ResultCodeEnum;
import org.lanjianghao.daijia.customer.client.CustomerInfoFeignClient;
import org.lanjianghao.daijia.customer.service.CustomerService;
import lombok.extern.slf4j.Slf4j;
import org.lanjianghao.daijia.model.form.customer.UpdateWxPhoneForm;
import org.lanjianghao.daijia.model.vo.customer.CustomerLoginVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class CustomerServiceImpl implements CustomerService {
    @Autowired
    private CustomerInfoFeignClient client;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public String login(String code) {
        Result<Long> loginRet = client.login(code);

        //判断获取用户是否成功
        Integer loginCode = loginRet.getCode();
        if (loginCode != 200) {
            throw new BusinessException(ResultCodeEnum.DATA_ERROR);
        }

        //判断用户id是否为空
        Long customerId = loginRet.getData();
        if (customerId == null) {
            throw new BusinessException(ResultCodeEnum.DATA_ERROR);
        }

        //生成随机token
        String token = UUID.randomUUID().toString().replaceAll("-", "");

        redisTemplate.opsForValue().set(RedisConstant.USER_LOGIN_KEY_PREFIX + token, customerId.toString(),
                                            RedisConstant.USER_LOGIN_KEY_TIMEOUT, TimeUnit.SECONDS);

        return token;
    }

    @Override
    public CustomerLoginVo getCustomerLoginInfo(Long customerId) {
        Result<CustomerLoginVo> ret = client.getCustomerLoginInfo(customerId);
        //判断获取用户是否成功
        if (ret.getCode() != 200 || ret.getData() == null) {
            throw new BusinessException(ResultCodeEnum.DATA_ERROR);
        }

        return ret.getData();
    }

    @Override
    public Boolean updateWxPhone(UpdateWxPhoneForm updateWxPhoneForm) {
        Result<Boolean> res = client.updateWxPhoneNumber(updateWxPhoneForm);
        return res.getData();
    }

}
