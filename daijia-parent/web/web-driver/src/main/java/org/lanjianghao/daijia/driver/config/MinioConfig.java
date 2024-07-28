package org.lanjianghao.daijia.driver.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MinioProperties.class)
public class MinioConfig {

    @Autowired
    MinioProperties props;

    @Bean
    public MinioClient minioClient() {
        // Create a minioClient with the MinIO server playground, its access key and secret key.
        return MinioClient.builder()
                .endpoint(props.getEndpointUrl())
                .credentials(props.getAccessKey(), props.getSecretKey())
                .build();
    }
}
