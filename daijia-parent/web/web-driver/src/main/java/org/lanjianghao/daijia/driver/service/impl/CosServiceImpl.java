package org.lanjianghao.daijia.driver.service.impl;

import org.lanjianghao.daijia.common.execption.BusinessException;
import org.lanjianghao.daijia.common.result.Result;
import org.lanjianghao.daijia.common.result.ResultCodeEnum;
import org.lanjianghao.daijia.driver.client.CosFeignClient;
import org.lanjianghao.daijia.driver.service.CosService;
import lombok.extern.slf4j.Slf4j;
import org.lanjianghao.daijia.model.vo.driver.CosUploadVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class CosServiceImpl implements CosService {

    @Autowired
    private CosFeignClient cosFeignClient;

    @Override
    public CosUploadVo updateFile(MultipartFile file, String path) {
        Result<CosUploadVo> res = cosFeignClient.upload(file, path);
        if (res.getCode() != 200) {
            throw new BusinessException(ResultCodeEnum.DATA_ERROR);
        }

        return res.getData();
    }
}
