// controllers/ProjectController.java
package com.onepagebuilder.backend.controller;

import com.onepagebuilder.backend.entity.Project;
import com.onepagebuilder.backend.services.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
            return ResponseEntity.notFound().build();
        }
    }
    
    // Save draft (auto-save)
    @PostMapping("/{projectId}/draft")
    public ResponseEntity<Void> saveDraft(
        @PathVariable Long projectId,
        @RequestHeader("User-Id") Long userId, // Or get from JWT token
        @RequestBody Object config
    ) {
        try {
            projectService.saveDraft(projectId, userId, config);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // Publish site
    @PostMapping("/{projectId}/publish")
    public ResponseEntity<Void> publishSite(
        @PathVariable Long projectId,
        @RequestHeader("User-Id") Long userId,
        @RequestBody Object config
    ) {
        try {
            projectService.publishSite(projectId, userId, config);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
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
            return ResponseEntity.notFound().build();
        }
    }
}