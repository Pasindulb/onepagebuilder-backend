package com.onepagebuilder.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
public class DigitalOceanConfig {

    @Value("${do.spaces.key}")
    private String spacesKey;

    @Value("${do.spaces.secret}")
    private String spacesSecret;

    @Value("${do.spaces.endpoint}")
    private String spacesEndpoint;

    @Value("${do.spaces.region}")
    private String spacesRegion;

    @Bean
    public S3Client s3Client() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(spacesKey, spacesSecret);

        return S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .endpointOverride(URI.create(spacesEndpoint))
                .region(Region.of(spacesRegion))
                .build();
    }
}
