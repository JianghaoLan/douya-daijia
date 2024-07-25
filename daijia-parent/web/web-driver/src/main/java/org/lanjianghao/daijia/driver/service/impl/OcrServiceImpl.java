package org.lanjianghao.daijia.driver.service.impl;

import org.lanjianghao.daijia.common.execption.BusinessException;
import org.lanjianghao.daijia.common.result.Result;
import org.lanjianghao.daijia.common.result.ResultCodeEnum;
import org.lanjianghao.daijia.driver.client.OcrFeignClient;
import org.lanjianghao.daijia.driver.service.OcrService;
import lombok.extern.slf4j.Slf4j;
import org.lanjianghao.daijia.model.vo.driver.DriverLicenseOcrVo;
import org.lanjianghao.daijia.model.vo.driver.IdCardOcrVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class OcrServiceImpl implements OcrService {

    @Autowired
    OcrFeignClient ocrFeignClient;

    @Override
    public IdCardOcrVo idCardOcr(MultipartFile file) {
        Result<IdCardOcrVo> ret = ocrFeignClient.idCardOcr(file);
        if (ret.getCode() != 200) {
            throw new BusinessException(ResultCodeEnum.DATA_ERROR);
        }
        return ret.getData();
    }

    @Override
    public DriverLicenseOcrVo driverLicenseOcr(MultipartFile file) {
        Result<DriverLicenseOcrVo> ret = ocrFeignClient.driverLicenseOcr(file);
        return ret.getData();
    }
}
