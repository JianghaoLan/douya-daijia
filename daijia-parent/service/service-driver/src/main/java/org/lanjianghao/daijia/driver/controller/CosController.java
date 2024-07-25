package org.lanjianghao.daijia.driver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.lanjianghao.daijia.common.result.Result;
import org.lanjianghao.daijia.driver.service.CosService;
import org.lanjianghao.daijia.model.vo.driver.CosUploadVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Tag(name = "腾讯云cos上传接口管理")
@RestController
@RequestMapping(value="/cos")
@SuppressWarnings({"unchecked", "rawtypes"})
public class CosController {

    @Autowired
    CosService cosService;

    @Operation(summary = "上传")
    @PostMapping("/upload")
    public Result<CosUploadVo> upload(@RequestPart("file") MultipartFile file, @RequestParam("path") String path) {
        CosUploadVo ret = cosService.upload(file, path);
        return Result.ok(ret);
    }

}

