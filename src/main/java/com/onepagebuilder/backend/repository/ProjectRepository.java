package com.onepagebuilder.backend.repository;
import com.onepagebuilder.backend.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByUserId(Long userId);
    Optional<Project> findBySlug(String slug);
    Optional<Project> findByIdAndUserId(Long id, Long userId);
    boolean existsBySlug(String slug);
}
