package com.example.b2b_opportunities.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmailDailyStats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(nullable = false, unique = true)
    private LocalDate day;

    @Builder.Default
    private Integer activationMailsSent = 0;

    @Builder.Default
    private Integer activationMailsOpened = 0;

    @Builder.Default
    private Integer newProjectMailsSent = 0;

    @Builder.Default
    private Integer newApplicationMailsSent = 0;
}
