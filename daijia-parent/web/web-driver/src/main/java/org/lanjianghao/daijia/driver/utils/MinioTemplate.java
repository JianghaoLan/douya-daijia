package org.lanjianghao.daijia.driver.utils;

import io.minio.*;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.lanjianghao.daijia.driver.config.MinioProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Slf4j
@Component
public class MinioTemplate {
    @Autowired
    private MinioClient minioClient;

    @Autowired
    private MinioProperties minioProps;

    public void makeBucketIfNotExists(String bucketName) {
        try {
            // Make bucket if not exist.
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                // Make a new bucket.
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
        } catch (MinioException e) {
            log.error("Error occurred: " + e + "HTTP trace: " + e.httpTrace());
            throw new RuntimeException(e);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    public String putObject(String bucketName, String objectName, MultipartFile file) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .build());
            return minioProps.getEndpointUrl() + "/" + bucketName + "/" + objectName;
        } catch (MinioException e) {
            log.error("Error occurred: " + e + "HTTP trace: " + e.httpTrace());
            throw new RuntimeException(e);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }
}
