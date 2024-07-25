package org.lanjianghao.daijia.driver.service.impl;

import org.lanjianghao.daijia.common.constant.RedisConstant;
import org.lanjianghao.daijia.common.execption.BusinessException;
import org.lanjianghao.daijia.common.result.Result;
import org.lanjianghao.daijia.common.result.ResultCodeEnum;
import org.lanjianghao.daijia.driver.client.DriverInfoFeignClient;
import org.lanjianghao.daijia.driver.service.DriverService;
import lombok.extern.slf4j.Slf4j;
import org.lanjianghao.daijia.model.entity.driver.DriverInfo;
import org.lanjianghao.daijia.model.form.driver.DriverFaceModelForm;
import org.lanjianghao.daijia.model.form.driver.UpdateDriverAuthInfoForm;
import org.lanjianghao.daijia.model.vo.driver.DriverAuthInfoVo;
import org.lanjianghao.daijia.model.vo.driver.DriverLoginVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class DriverServiceImpl implements DriverService {

    @Autowired
    DriverInfoFeignClient client;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Override
    public String login(String code) {

        Result<Long> loginRes = client.login(code);
        if (loginRes.getCode() != 200 || loginRes.getData() == null) {
            throw new BusinessException(ResultCodeEnum.DATA_ERROR);
        }

        Long driverId = loginRes.getData();

        //生成随机token
        String token = UUID.randomUUID().toString().replaceAll("-", "");

        redisTemplate.opsForValue().set(RedisConstant.USER_LOGIN_KEY_PREFIX + token, driverId.toString(),
                                        RedisConstant.USER_LOGIN_KEY_TIMEOUT, TimeUnit.SECONDS);

        return token;
    }

    @Override
    public DriverLoginVo getDriverLoginInfo(Long driverId) {
        Result<DriverLoginVo> result = client.getDriverLoginInfo(driverId);
        if (result.getCode() != 200) {
            throw new BusinessException(ResultCodeEnum.DATA_ERROR);
        }

        return result.getData();
    }

    @Override
    public DriverAuthInfoVo getDriverAuthInfo(Long driverId) {
        return client.getDriverAuthInfo(driverId).getData();
    }

    @Override
    public boolean updateDriverAuthInfo(UpdateDriverAuthInfoForm updateDriverAuthInfoForm) {
        return client.updateDriverAuthInfo(updateDriverAuthInfoForm).getData();
    }

    @Override
    public Boolean creatDriverFaceModel(DriverFaceModelForm driverFaceModelForm) {
        return client.createDriverFaceModel(driverFaceModelForm).getData();
    }
}
