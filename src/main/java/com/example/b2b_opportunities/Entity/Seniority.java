package com.example.b2b_opportunities.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
@Table(name = "seniorities")
public class Seniority {
    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @NotNull
    private String label;

    @NotNull
    private short level;
}