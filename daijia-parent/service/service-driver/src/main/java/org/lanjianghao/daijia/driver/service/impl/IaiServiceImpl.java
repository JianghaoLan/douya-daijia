package org.lanjianghao.daijia.driver.service.impl;

import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.iai.v20200303.IaiClient;
import com.tencentcloudapi.iai.v20200303.models.CreatePersonRequest;
import com.tencentcloudapi.iai.v20200303.models.CreatePersonResponse;
import org.lanjianghao.daijia.driver.config.TencentCloudConfigProperties;
import org.lanjianghao.daijia.driver.service.IaiService;
import org.lanjianghao.daijia.model.entity.driver.DriverInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IaiServiceImpl implements IaiService {

    @Autowired
    private IaiClient iaiClient;

    @Autowired
    private TencentCloudConfigProperties configProps;

    @Override
    public String createDriverFaceModel(DriverInfo driverInfo, String imageBase64) {
        CreatePersonRequest req = new CreatePersonRequest();
        req.setGroupId(configProps.getPersonGroupId());
        req.setPersonName(driverInfo.getName());
        req.setUniquePersonControl(4L);
        req.setQualityControl(4L);
        req.setPersonId(String.valueOf(driverInfo.getId()));
        req.setGender(Long.parseLong(driverInfo.getGender()));
        req.setImage(imageBase64);
        // 返回的resp是一个CreatePersonResponse的实例，与请求对象对应
        try {
            CreatePersonResponse resp = iaiClient.CreatePerson(req);
            return resp.getFaceId();
        } catch (TencentCloudSDKException e) {
            throw new RuntimeException(e);
        }
    }
}
