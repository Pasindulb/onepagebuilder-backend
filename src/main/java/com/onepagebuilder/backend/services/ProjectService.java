// services/ProjectService.java
package com.onepagebuilder.backend.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onepagebuilder.backend.entity.Project;
import com.onepagebuilder.backend.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {
    
    private final ProjectRepository projectRepository;
    private final ObjectMapper objectMapper;
    
    // Create new project
    @Transactional
    public Project createProject(Long userId, String name, String description) {
        Project project = new Project();
        project.setUserId(userId);
        project.setName(name);
        project.setSlug(generateUniqueSlug(name));
        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());
        
        // Initialize with empty config
        String emptyConfig = "{}";
        project.setDraftConfig(emptyConfig);
        project.setDraftUpdatedAt(LocalDateTime.now());
        
        return projectRepository.save(project);
    }
    
    // Generate unique slug
    private String generateUniqueSlug(String name) {
        String baseSlug = name.toLowerCase()
            .replaceAll("[^a-z0-9\\s-]", "")
            .replaceAll("\\s+", "-")
            .replaceAll("-+", "-")
            .trim();
        
        if (baseSlug.isEmpty()) {
            baseSlug = "project";
        }
        
        String slug = baseSlug;
        int counter = 1;
        
        while (projectRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + counter;
            counter++;
        }
        
        return slug;
    }
    
    // Get single project
    public Project getProject(Long projectId, Long userId) {
        return projectRepository.findByIdAndUserId(projectId, userId)
            .orElseThrow(() -> new RuntimeException("Project not found"));
    }
    
    // Get user's projects
    public List<Project> getUserProjects(Long userId) {
        return projectRepository.findByUserId(userId);
    }
    
    // Save draft (auto-save)
    @Transactional
    public void saveDraft(Long projectId, Long userId, Object config) throws Exception {
        Project project = projectRepository.findByIdAndUserId(projectId, userId)
            .orElseThrow(() -> new RuntimeException("Project not found"));
        
        String configJson = objectMapper.writeValueAsString(config);
        project.setDraftConfig(configJson);
        project.setDraftUpdatedAt(LocalDateTime.now());
        
        projectRepository.save(project);
    }
    
    // Publish site
    @Transactional
    public void publishSite(Long projectId, Long userId, Object config) throws Exception {
        Project project = projectRepository.findByIdAndUserId(projectId, userId)
            .orElseThrow(() -> new RuntimeException("Project not found"));
        
        String configJson = objectMapper.writeValueAsString(config);
        
        // Save as published version
        project.setPublishedConfig(configJson);
        project.setPublishedAt(LocalDateTime.now());
        
        // Also update draft to match
        project.setDraftConfig(configJson);
        project.setDraftUpdatedAt(LocalDateTime.now());
        
        projectRepository.save(project);
    }
    
    // Get draft
    public String getDraft(Long projectId, Long userId) {
        Project project = projectRepository.findByIdAndUserId(projectId, userId)
            .orElseThrow(() -> new RuntimeException("Project not found"));
        return project.getDraftConfig();
    }
    
    // Get published
    public String getPublished(Long projectId) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new RuntimeException("Project not found"));
        return project.getPublishedConfig();
    }
}