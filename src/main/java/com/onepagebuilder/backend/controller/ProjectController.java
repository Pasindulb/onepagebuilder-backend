// controllers/ProjectController.java
package com.onepagebuilder.backend.controller;

import com.onepagebuilder.backend.entity.Project;
import com.onepagebuilder.backend.services.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000") // Your React app
public class ProjectController {
    
    private final ProjectService projectService;
    
    // Create new project
    @PostMapping
    public ResponseEntity<Project> createProject(
        @RequestHeader("User-Id") Long userId,
        @RequestBody Map<String, String> request
    ) {
        try {
            String name = request.get("name");
            String description = request.getOrDefault("description", "");
            
            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            Project project = projectService.createProject(userId, name, description);
            return ResponseEntity.ok(project);
        } catch (Exception e) {
            log.error("Error creating project for user: {}", userId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // Get all projects for user
    @GetMapping
    public ResponseEntity<List<Project>> getUserProjects(@RequestHeader("User-Id") Long userId) {
        try {
            List<Project> projects = projectService.getUserProjects(userId);
            return ResponseEntity.ok(projects);
        } catch (Exception e) {
            log.error("Error fetching projects for user: {}", userId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // Get single project
    @GetMapping("/{projectId}")
    public ResponseEntity<Project> getProject(
        @PathVariable Long projectId,
        @RequestHeader("User-Id") Long userId
    ) {
        try {
            Project project = projectService.getProject(projectId, userId);
            return ResponseEntity.ok(project);
        } catch (Exception e) {
            log.error("Error fetching project {} for user: {}", projectId, userId, e);
            return ResponseEntity.notFound().build();
        }
    }
    
    // Save draft (auto-save)
    @PostMapping("/{projectId}/draft")
    public ResponseEntity<Void> saveDraft(
        @PathVariable Long projectId,
        @RequestHeader("User-Id") Long userId,
        @RequestBody Object config
    ) {
        try {
            projectService.saveDraft(projectId, userId, config);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error saving draft for project {} user: {}", projectId, userId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // Publish site
    @PostMapping("/{projectId}/publish")
    public ResponseEntity<Map<String, String>> publishSite(
        @PathVariable Long projectId,
        @RequestHeader("User-Id") Long userId,
        @RequestBody Object config
    ) {
        try {
            log.info("Publishing site - Project: {}, User: {}", projectId, userId);
            String liveUrl = projectService.publishSite(projectId, userId, config);
            log.info("Site published successfully - Project: {}, URL: {}", projectId, liveUrl);
            return ResponseEntity.ok(Map.of(
                "message", "Site published successfully",
                "liveUrl", liveUrl
            ));
        } catch (Exception e) {
            log.error("Error publishing site for project {} user: {}", projectId, userId, e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to publish site",
                "message", e.getMessage()
            ));
        }
    }
    
    // Get draft
    @GetMapping("/{projectId}/draft")
    public ResponseEntity<String> getDraft(
        @PathVariable Long projectId,
        @RequestHeader("User-Id") Long userId
    ) {
        try {
            String draft = projectService.getDraft(projectId, userId);
            return ResponseEntity.ok(draft);
        } catch (Exception e) {
            log.error("Error fetching draft for project {} user: {}", projectId, userId, e);
            return ResponseEntity.notFound().build();
        }
    }
    
    // Get published
    @GetMapping("/{projectId}/published")
    public ResponseEntity<String> getPublished(@PathVariable Long projectId) {
        try {
            String published = projectService.getPublished(projectId);
            return ResponseEntity.ok(published);
        } catch (Exception e) {
            log.error("Error fetching published site for project: {}", projectId, e);
            return ResponseEntity.notFound().build();
        }
    }
}