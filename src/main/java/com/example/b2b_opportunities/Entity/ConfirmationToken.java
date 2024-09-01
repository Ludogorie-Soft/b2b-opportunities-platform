package com.example.b2b_opportunities.Entity;

import com.example.b2b_opportunities.Entity.Employer;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
@Entity
public class ConfirmationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(nullable = false)
    private String token;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(
            nullable = false,
            name = "employer_id"
    )
    private Employer employer;

    public ConfirmationToken(String token, LocalDateTime createdAt, Employer employer) {
        this.token = token;
        this.createdAt = createdAt;
        this.employer = employer;
    }
}
