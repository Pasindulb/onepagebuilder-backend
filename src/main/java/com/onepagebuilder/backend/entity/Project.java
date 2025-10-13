package com.onepagebuilder.backend.entity;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import lombok.Data;
import org.hibernate.annotations.Type;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "projects")
@Data
public class Project {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(nullable = false)
    private String name;
    
    @Column(unique = true)
    private String slug;
    
    @Type(JsonBinaryType.class)
    @Column(name = "draft_config", columnDefinition = "jsonb")
    private String draftConfig; // Store as JSON string
    
    @Column(name = "draft_updated_at")
    private LocalDateTime draftUpdatedAt;
    
    @Type(JsonBinaryType.class)
    @Column(name = "published_config", columnDefinition = "jsonb")
    private String publishedConfig;
    
    @Column(name = "published_at")
    private LocalDateTime publishedAt;
    
    @Column(name = "live_url")
    private String liveUrl;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}