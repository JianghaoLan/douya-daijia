package org.lanjianghao.daijia.driver.service.impl;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import io.minio.errors.MinioException;
import org.lanjianghao.daijia.driver.config.MinioProperties;
import org.lanjianghao.daijia.driver.service.CosService;
import org.lanjianghao.daijia.driver.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.lanjianghao.daijia.driver.utils.MinioTemplate;
import org.lanjianghao.daijia.model.vo.driver.CosUploadVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class FileServiceImpl implements FileService {

    @Autowired
    private MinioProperties minioProps;

    @Autowired
    private MinioTemplate minioTemplate;

    private String getExt(String oriFilename) {
        if (!StringUtils.hasText(oriFilename)) {
            return "";
        }
        return oriFilename.substring(oriFilename.lastIndexOf("."));
    }

    private String generateUniqueFilename(String oriFilename) {
        // 设置存储对象名称
        String extFileName = getExt(oriFilename);
        return new SimpleDateFormat("yyyyMMdd").format(new Date())
                + "/" + UUID.randomUUID().toString().replace("-" , "") + "." + extFileName;
    }

    @Override
    public String upload(MultipartFile file) {

        minioTemplate.makeBucketIfNotExists(minioProps.getBucketName());

        String filename = generateUniqueFilename(file.getOriginalFilename());

        return minioTemplate.putObject(minioProps.getBucketName(), filename, file);

    }
}
