package org.lanjianghao.daijia.driver.service.impl;

import com.tencentcloudapi.common.AbstractModel;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.ocr.v20181119.OcrClient;
import com.tencentcloudapi.ocr.v20181119.models.DriverLicenseOCRRequest;
import com.tencentcloudapi.ocr.v20181119.models.DriverLicenseOCRResponse;
import com.tencentcloudapi.ocr.v20181119.models.IDCardOCRRequest;
import com.tencentcloudapi.ocr.v20181119.models.IDCardOCRResponse;
import org.apache.commons.codec.binary.Base64;
import org.joda.time.format.DateTimeFormat;
import org.lanjianghao.daijia.driver.service.CosService;
import org.lanjianghao.daijia.driver.service.OcrService;
import lombok.extern.slf4j.Slf4j;
import org.lanjianghao.daijia.model.vo.driver.CosUploadVo;
import org.lanjianghao.daijia.model.vo.driver.DriverLicenseOcrVo;
import org.lanjianghao.daijia.model.vo.driver.IdCardOcrVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class OcrServiceImpl implements OcrService {

    @Autowired
    private OcrClient ocrClient;

    @Autowired
    private CosService cosService;

    @Override
    public IdCardOcrVo idCardOcr(MultipartFile file) {
        // 实例化一个请求对象,每个接口都会对应一个request对象
        IDCardOCRRequest req = new IDCardOCRRequest();

        String fileBase64;
        try {
            byte[] bytes = Base64.encodeBase64(file.getBytes());
            fileBase64 = new String(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        req.setImageBase64(fileBase64);

        // 返回的resp是一个IDCardOCRResponse的实例，与请求对象对应
        IDCardOCRResponse resp;
        try {
            resp = ocrClient.IDCardOCR(req);
        } catch (TencentCloudSDKException e) {
            throw new RuntimeException(e);
        }
//        // 输出json格式的字符串回包
//        System.out.println(AbstractModel.toJsonString(resp));

        //转换为IdCardOcrVo对象
        IdCardOcrVo idCardOcrVo = new IdCardOcrVo();
        if (StringUtils.hasText(resp.getName())) {
            //身份证正面
            idCardOcrVo.setName(resp.getName());
            idCardOcrVo.setGender("男".equals(resp.getSex()) ? "1" : "2");
            idCardOcrVo.setBirthday(DateTimeFormat.forPattern("yyyy/MM/dd").parseDateTime(resp.getBirth()).toDate());
            idCardOcrVo.setIdcardNo(resp.getIdNum());
            idCardOcrVo.setIdcardAddress(resp.getAddress());

            //上传身份证正面图片到腾讯云cos
            CosUploadVo cosUploadVo = cosService.upload(file, "idCard");
            idCardOcrVo.setIdcardFrontUrl(cosUploadVo.getUrl());
            idCardOcrVo.setIdcardFrontShowUrl(cosUploadVo.getShowUrl());
        } else {
            //身份证反面
            //证件有效期："2010.07.21-2020.07.21"
            String idcardExpireString = resp.getValidDate().split("-")[1];
            idCardOcrVo.setIdcardExpire(DateTimeFormat.forPattern("yyyy.MM.dd").parseDateTime(idcardExpireString).toDate());
            //上传身份证反面图片到腾讯云cos
            CosUploadVo cosUploadVo = cosService.upload(file, "idCard");
            idCardOcrVo.setIdcardBackUrl(cosUploadVo.getUrl());
            idCardOcrVo.setIdcardBackShowUrl(cosUploadVo.getShowUrl());
        }
        return idCardOcrVo;
    }

    @Override
    public DriverLicenseOcrVo driverLicenseOcr(MultipartFile file) {
        DriverLicenseOCRRequest req = new DriverLicenseOCRRequest();

        String fileBase64;
        try {
            byte[] bytes = Base64.encodeBase64(file.getBytes());
            fileBase64 = new String(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        req.setImageBase64(fileBase64);

        // 返回的resp是一个DriverLicenseOCRResponse的实例，与请求对象对应
        DriverLicenseOCRResponse resp;
        try {
            resp = ocrClient.DriverLicenseOCR(req);
        } catch (TencentCloudSDKException e) {
            throw new RuntimeException(e);
        }
//        // 输出json格式的字符串回包
//        System.out.println(AbstractModel.toJsonString(resp));

        DriverLicenseOcrVo driverLicenseOcrVo = new DriverLicenseOcrVo();
        if (StringUtils.hasText(resp.getName())) {
            //驾驶证正面
            //驾驶证名称要与身份证名称一致
            driverLicenseOcrVo.setName(resp.getName());
            driverLicenseOcrVo.setDriverLicenseClazz(resp.getClass_());
            driverLicenseOcrVo.setDriverLicenseNo(resp.getCardCode());
            driverLicenseOcrVo.setDriverLicenseIssueDate(DateTimeFormat.forPattern("yyyy-MM-dd").parseDateTime(resp.getDateOfFirstIssue()).toDate());
            driverLicenseOcrVo.setDriverLicenseExpire(DateTimeFormat.forPattern("yyyy-MM-dd").parseDateTime(resp.getEndDate()).toDate());

            //上传驾驶证反面图片到腾讯云cos
            CosUploadVo cosUploadVo = cosService.upload(file, "driverLicense");
            driverLicenseOcrVo.setDriverLicenseFrontUrl(cosUploadVo.getUrl());
            driverLicenseOcrVo.setDriverLicenseFrontShowUrl(cosUploadVo.getShowUrl());
        } else {
            //驾驶证反面
            //上传驾驶证反面图片到腾讯云cos
            CosUploadVo cosUploadVo = cosService.upload(file, "driverLicense");
            driverLicenseOcrVo.setDriverLicenseBackUrl(cosUploadVo.getUrl());
        }

        return driverLicenseOcrVo;
    }
}
