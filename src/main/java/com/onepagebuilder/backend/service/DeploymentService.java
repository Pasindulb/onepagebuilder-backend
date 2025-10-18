package com.onepagebuilder.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.nio.charset.StandardCharsets;

@Service
public class DeploymentService {

    @Autowired
    private S3Client s3Client;

    @Autowired
    private StaticSiteGeneratorService siteGenerator;

    @Value("${do.spaces.bucket}")
    private String bucketName;

    @Value("${do.spaces.cdn-endpoint}")
    private String cdnEndpoint;

    public String deploySite(String username, String projectName, String publishedConfig) {
        try {
            // Generate static HTML
            String html = siteGenerator.generateStaticHTML(projectName, publishedConfig);
            
            // Create folder structure: username/index.html
            String key = username + "/index.html";

            // Upload to DigitalOcean Spaces
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType("text/html")
                    .cacheControl("no-cache")
                    .acl(ObjectCannedACL.PUBLIC_READ) // Make publicly accessible
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromString(html, StandardCharsets.UTF_8));

            // Return the public URL
            String siteUrl = "https://" + username + ".onepagebuilder.live";
            
            System.out.println("===== DEPLOYMENT SUCCESS =====");
            System.out.println("Project: " + projectName);
            System.out.println("Username: " + username);
            System.out.println("Deployed to: " + key);
            System.out.println("Live URL: " + siteUrl);
            System.out.println("==============================");
            
            return siteUrl;

        } catch (Exception e) {
            throw new RuntimeException("Failed to deploy site: " + e.getMessage(), e);
        }
    }

    public void deleteSite(String username) {
        try {
            String key = username + "/index.html";
            
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteRequest);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete site: " + e.getMessage(), e);
        }
    }

    public boolean siteExists(String username) {
        try {
            String key = username + "/index.html";
            
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.headObject(headRequest);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (Exception e) {
            throw new RuntimeException("Failed to check site existence: " + e.getMessage(), e);
        }
    }
}
