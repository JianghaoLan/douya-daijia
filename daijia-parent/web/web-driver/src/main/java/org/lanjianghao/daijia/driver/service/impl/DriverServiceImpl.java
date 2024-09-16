package org.lanjianghao.daijia.driver.service.impl;

import org.lanjianghao.daijia.common.constant.RedisConstant;
import org.lanjianghao.daijia.common.execption.BusinessException;
import org.lanjianghao.daijia.common.result.Result;
import org.lanjianghao.daijia.common.result.ResultCodeEnum;
import org.lanjianghao.daijia.dispatch.client.NewOrderFeignClient;
import org.lanjianghao.daijia.driver.client.DriverInfoFeignClient;
import org.lanjianghao.daijia.driver.service.DriverService;
import lombok.extern.slf4j.Slf4j;
import org.lanjianghao.daijia.map.client.LocationFeignClient;
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

    @Autowired
    LocationFeignClient locationFeignClient;

    @Autowired
    private NewOrderFeignClient newOrderFeignClient;

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

    @Override
    public Boolean isFaceRecognition(Long driverId) {
        return client.isFaceRecognition(driverId).getData();
    }

    @Override
    public Boolean verifyDriverFace(DriverFaceModelForm driverFaceModelForm) {
        return client.verifyDriverFace(driverFaceModelForm).getData();
    }

    @Override
    public Boolean startService(Long driverId) {
        //判断是否完成认证
        DriverLoginVo loginInfo = client.getDriverLoginInfo(driverId).getData();
        if (loginInfo.getAuthStatus() != 2) {
            throw new BusinessException(ResultCodeEnum.AUTH_ERROR);
        }

        //判断当天是否完成人脸识别
        Boolean isFaceRec = client.isFaceRecognition(driverId).getData();
        if (!isFaceRec) {
            throw new BusinessException(ResultCodeEnum.FACE_ERROR);
        }

        //更新订单状态为接单中
        client.updateServiceStatus(driverId, 1);

        //清除代驾位置信息
        locationFeignClient.removeDriverLocation(driverId);

//        //清空代驾订单队列信息
//        newOrderFeignClient.clearNewOrderQueueData(driverId);

        return true;
    }

    @Override
    public Boolean stopService(Long driverId) {
        //更新司机状态为未结单
        client.updateServiceStatus(driverId, 0);

        //清除代驾位置信息
        locationFeignClient.removeDriverLocation(driverId);

//        //清空代驾订单队列信息
//        newOrderFeignClient.clearNewOrderQueueData(driverId);

        return true;
    }
}
