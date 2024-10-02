package com.example.b2b_opportunities.Repository;

import com.example.b2b_opportunities.Entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
}
