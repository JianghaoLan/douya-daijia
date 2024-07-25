package org.lanjianghao.daijia.driver.service;

import org.lanjianghao.daijia.model.vo.driver.CosUploadVo;
import org.springframework.web.multipart.MultipartFile;

public interface CosService {
    CosUploadVo updateFile(MultipartFile file, String path);
}
