package org.lanjianghao.daijia.driver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.lanjianghao.daijia.common.login.LoginRequired;
import org.lanjianghao.daijia.common.result.Result;
import org.lanjianghao.daijia.driver.service.CosService;
import org.lanjianghao.daijia.driver.service.FileService;
import org.lanjianghao.daijia.model.vo.driver.CosUploadVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "上传管理接口")
@RestController
@RequestMapping("file")
public class FileController {

//    @Autowired
//    private CosService cosService;

//    //文件上传接口
//    @Operation(summary = "上传")
//    //@GuiguLogin
//    @PostMapping("/upload")
//    public Result<String> upload(@RequestPart("file") MultipartFile file,
//                                      @RequestParam(name = "path",defaultValue = "auth") String path) {
//        CosUploadVo cosUploadVo = cosService.uploadFile(file,path);
//        return Result.ok(cosUploadVo.getUrl());
//    }

    @Autowired
    private FileService fileService;

    //文件上传接口
    @Operation(summary = "上传")
    //@GuiguLogin
    @PostMapping("/upload")
    public Result<String> upload(@RequestPart("file") MultipartFile file) {
        String url = fileService.upload(file);
        return Result.ok(url);
    }
}