package org.lanjianghao.daijia.driver.service.impl;

import com.tencentcloudapi.common.AbstractModel;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.iai.v20200303.IaiClient;
import com.tencentcloudapi.iai.v20200303.models.*;
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

    public Boolean detectLiveFace(String imageBase64, Long driverId) {
        // 实例化一个请求对象,每个接口都会对应一个request对象
        DetectLiveFaceRequest req = new DetectLiveFaceRequest();
        req.setImage(imageBase64);
        // 返回的resp是一个DetectLiveFaceResponse的实例，与请求对象对应
        DetectLiveFaceResponse resp;
        try {
            resp = iaiClient.DetectLiveFace(req);
        } catch (TencentCloudSDKException e) {
            throw new RuntimeException(e);
        }
        return resp.getIsLiveness();
    }

    public Boolean verifyFace(String imageBase64, Long driverId) {
        VerifyFaceRequest req = new VerifyFaceRequest();
        req.setImage(imageBase64);
        req.setPersonId(String.valueOf(driverId));
        // 返回的resp是一个VerifyFaceResponse的实例，与请求对象对应
        VerifyFaceResponse resp;
        try {
            resp = iaiClient.VerifyFace(req);
        } catch (TencentCloudSDKException e) {
            throw new RuntimeException(e);
        }

        return resp.getIsMatch();
    }
}
