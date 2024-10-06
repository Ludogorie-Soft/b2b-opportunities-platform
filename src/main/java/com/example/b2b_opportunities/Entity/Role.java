package com.example.b2b_opportunities.Entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "roles")
public class Role {
    @Id
    private Long id;

    @Column(unique = true)
    @NotBlank
    @Size(min = 1, max = 40)
    private String name;

    public void setName(@NotBlank @Size(min = 1, max = 40) String name) {
        this.name = name.strip();
    }
}