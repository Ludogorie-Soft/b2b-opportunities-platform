package com.example.b2b_opportunities.Repository;

import com.example.b2b_opportunities.Entity.Project;
import com.example.b2b_opportunities.Static.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByProjectStatus(ProjectStatus projectStatus);
}
