package com.example.b2b_opportunities.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "seniorities")
public class Seniority {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    private String identifier;

    @NotNull
    private String label;

    @NotNull
    private short level;
}