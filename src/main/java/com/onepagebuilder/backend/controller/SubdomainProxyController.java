package com.onepagebuilder.backend.controller;

import com.onepagebuilder.backend.entity.User;
import com.onepagebuilder.backend.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class SubdomainProxyController {

    @Value("${do.spaces.bucket}")
    private String bucketName;

    @Autowired
    private S3Client s3Client;

    @Autowired
    private UserRepository userRepository;

    // MIME type mapping for common file extensions
    private static final Map<String, String> MIME_TYPES = new HashMap<>();
    static {
        MIME_TYPES.put(".html", "text/html");
        MIME_TYPES.put(".css", "text/css");
        MIME_TYPES.put(".js", "application/javascript");
        MIME_TYPES.put(".json", "application/json");
        MIME_TYPES.put(".png", "image/png");
        MIME_TYPES.put(".jpg", "image/jpeg");
        MIME_TYPES.put(".jpeg", "image/jpeg");
        MIME_TYPES.put(".gif", "image/gif");
        MIME_TYPES.put(".svg", "image/svg+xml");
        MIME_TYPES.put(".ico", "image/x-icon");
        MIME_TYPES.put(".woff", "font/woff");
        MIME_TYPES.put(".woff2", "font/woff2");
        MIME_TYPES.put(".ttf", "font/ttf");
        MIME_TYPES.put(".eot", "application/vnd.ms-fontobject");
    }

    /**
     * Catch-all endpoint to serve published sites
     * 
     * How it works:
     * 1. User visits the site
     * 2. Spring Boot fetches username directly from database (first user's email)
     * 3. Extracts username from email (e.g., "pasindu@example.com" → "pasindu")
     * 4. Spring Boot fetches from S3: {username}/index.html
     * 5. Spring Boot serves it back to the user
     * 
     * Also handles static assets like CSS, JS, images, etc.
     */
    @GetMapping(value = "/**")
    public ResponseEntity<?> servePublishedSite(
            @RequestHeader(value = "Host", required = false) String host,
            HttpServletRequest request
    ) {
        try {
            // Skip API endpoints
            if (request.getRequestURI().startsWith("/api/")) {
                return null; // Let other controllers handle it
            }

            // Fetch username directly from database (gets first user's email and extracts username)
            String username = getUsernameFromDatabase();
            if (username == null) {
                System.err.println("No user found in database");
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Site not found. No user registered.");
            }

            // Get the requested path (default to index.html for root)
            String requestPath = request.getRequestURI();
            if (requestPath.equals("/") || requestPath.isEmpty()) {
                requestPath = "/index.html";
            }

            // Build S3 key: username/path
            String s3Key = username + requestPath;

            System.out.println("===== SITE PROXY REQUEST (DB LOOKUP) =====");
            System.out.println("Username from DB: " + username);
            System.out.println("Request Path: " + requestPath);
            System.out.println("S3 Key: " + s3Key);
            System.out.println("==========================================");

            // Fetch from S3
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest);
            byte[] content = s3Object.readAllBytes();
            s3Object.close();

            // Determine content type
            String contentType = determineContentType(requestPath);

            // Return with proper headers
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .header(HttpHeaders.CACHE_CONTROL, "public, max-age=3600")
                    .header("X-Served-By", "OnePageBuilder-Proxy")
                    .body(new ByteArrayResource(content));

        } catch (NoSuchKeyException e) {
            System.err.println("File not found in S3: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Site not found. Please check if the site has been published.");
        } catch (IOException e) {
            System.err.println("Error reading S3 object: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error loading site content");
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred");
        }
    }

    /**
     * Fetch username directly from database
     * Gets the first user's email and extracts username from it
     */
    private String getUsernameFromDatabase() {
        List<User> users = userRepository.findAll();
        
        if (users.isEmpty()) {
            return null;
        }
        
        // Get first user's email and extract username
        User user = users.get(0);
        return extractUsername(user.getEmail());
    }

    /**
     * Extract username from email (same logic as ProjectService)
     * Example: "pasindu.bandara@example.com" → "pasindu-bandara"
     */
    private String extractUsername(String email) {
        return email.split("@")[0]
            .toLowerCase()
            .replaceAll("[^a-z0-9]", "-")
            .replaceAll("-+", "-")
            .trim();
    }

    /**
     * Determine MIME type based on file extension
     */
    private String determineContentType(String path) {
        String lowerPath = path.toLowerCase();
        
        for (Map.Entry<String, String> entry : MIME_TYPES.entrySet()) {
            if (lowerPath.endsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        
        // Default to octet-stream for unknown types
        return MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }
}
