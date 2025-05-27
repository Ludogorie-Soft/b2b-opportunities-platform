package com.example.b2b_opportunities.entity;


import com.example.b2b_opportunities.enums.ApplicationStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "position_applications")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PositionApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "position_id")
    private Position position;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company talentCompany;

    @ManyToOne
    @JoinColumn(name = "talent_id")
    private Talent talent;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus applicationStatus;

    private int rate;
    private LocalDateTime applicationDateTime;
    private LocalDateTime lastUpdateDateTime;
    private LocalDateTime availableFrom;
}
