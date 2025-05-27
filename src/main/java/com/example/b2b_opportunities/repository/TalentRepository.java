package com.example.b2b_opportunities.repository;

import com.example.b2b_opportunities.entity.Talent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TalentRepository extends JpaRepository<Talent, Long>, CustomTalentRepository{
    List<Talent> findByCompanyId(Long companyId);

}
