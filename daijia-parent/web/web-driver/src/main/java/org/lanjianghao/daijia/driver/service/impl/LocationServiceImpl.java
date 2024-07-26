package org.lanjianghao.daijia.driver.service.impl;

import org.lanjianghao.daijia.common.execption.BusinessException;
import org.lanjianghao.daijia.common.result.Result;
import org.lanjianghao.daijia.common.result.ResultCodeEnum;
import org.lanjianghao.daijia.driver.client.DriverInfoFeignClient;
import org.lanjianghao.daijia.driver.service.LocationService;
import lombok.extern.slf4j.Slf4j;
import org.lanjianghao.daijia.map.client.LocationFeignClient;
import org.lanjianghao.daijia.map.client.MapFeignClient;
import org.lanjianghao.daijia.model.entity.driver.DriverInfo;
import org.lanjianghao.daijia.model.entity.driver.DriverSet;
import org.lanjianghao.daijia.model.form.map.UpdateDriverLocationForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class LocationServiceImpl implements LocationService {

    @Autowired
    private LocationFeignClient locationFeignClient;

    @Autowired
    private DriverInfoFeignClient driverInfoFeignClient;

    @Override
    public Boolean updateDriverLocation(UpdateDriverLocationForm updateDriverLocationForm) {

        //检查司机是否开启接单
        Result<DriverSet> driverSetRes = driverInfoFeignClient.getDriverSet(updateDriverLocationForm.getDriverId());
        DriverSet driverSet = driverSetRes.getData();
        if (driverSet.getServiceStatus() == 1) {
            //更新位置信息
            return locationFeignClient.updateDriverLocation(updateDriverLocationForm).getData();
        }
        throw new BusinessException(ResultCodeEnum.NO_START_SERVICE);
    }
}
