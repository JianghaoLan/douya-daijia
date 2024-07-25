package org.lanjianghao.daijia.driver.service;

import org.lanjianghao.daijia.model.vo.driver.DriverLicenseOcrVo;
import org.lanjianghao.daijia.model.vo.driver.IdCardOcrVo;
import org.springframework.web.multipart.MultipartFile;

public interface OcrService {

    IdCardOcrVo idCardOcr(MultipartFile file);

    DriverLicenseOcrVo driverLicenseOcr(MultipartFile file);
}
