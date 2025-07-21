package com.example.b2b_opportunities.repository;

import com.example.b2b_opportunities.entity.EmailDailyStats;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Optional;

public interface EmailDailyStatsRepository extends JpaRepository<EmailDailyStats, Long> {
    Optional<EmailDailyStats> findByDay(LocalDate day);

    Page<EmailDailyStats> findAllByOrderByDayDesc(Pageable pageable);
}