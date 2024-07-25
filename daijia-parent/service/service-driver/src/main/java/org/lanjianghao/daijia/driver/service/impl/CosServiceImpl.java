package org.lanjianghao.daijia.driver.service.impl;

import com.alibaba.fastjson2.JSON;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.http.HttpMethodName;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.StorageClass;
import org.lanjianghao.daijia.driver.config.TencentCloudConfigProperties;
import org.lanjianghao.daijia.driver.service.CosService;
import lombok.extern.slf4j.Slf4j;
import org.lanjianghao.daijia.model.vo.driver.CosUploadVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class CosServiceImpl implements CosService {

    @Autowired
    TencentCloudConfigProperties props;

    @Autowired
    COSClient cosClient;

    @Override
    public CosUploadVo upload(MultipartFile file, String path) {
        //元数据信息
        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentLength(file.getSize());
        meta.setContentEncoding("UTF-8");
        meta.setContentType(file.getContentType());

        //向存储桶中保存文件
        String fileType = Objects.requireNonNull(
                file.getOriginalFilename()).substring(file.getOriginalFilename().lastIndexOf(".")); //文件后缀名
        String uploadPath = "/driver/" + path + "/" + UUID.randomUUID().toString().replaceAll("-", "") + fileType;
        PutObjectRequest putObjectRequest;
        try {
            putObjectRequest = new PutObjectRequest(props.getBucketPrivate(), uploadPath, file.getInputStream(), meta);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        putObjectRequest.setStorageClass(StorageClass.Standard);
        PutObjectResult ret = cosClient.putObject(putObjectRequest);//上传文件

        String url = getImageUrl(uploadPath);

        //封装返回对象
        CosUploadVo cosUploadVo = new CosUploadVo();
        cosUploadVo.setUrl(uploadPath);
        //图片临时访问url，回显使用
        cosUploadVo.setShowUrl(url);
        return cosUploadVo;
    }

    @Override
    public String getImageUrl(String path) {
        if (!StringUtils.hasLength(path)) {
            return "";
        }

        // 存储桶的命名格式为 BucketName-APPID，此处填写的存储桶名称必须为此格式
        String bucketName = props.getBucketPrivate();

        // 设置签名过期时间(可选), 若未进行设置则默认使用 ClientConfig 中的签名过期时间(1小时)
        // 这里设置签名在半个小时后过期
        Date expirationDate = new Date(System.currentTimeMillis() + 15 * 60 * 1000);

        URL url = cosClient.generatePresignedUrl(bucketName, path, expirationDate, HttpMethodName.GET);

        return url.toString();
    }
}
