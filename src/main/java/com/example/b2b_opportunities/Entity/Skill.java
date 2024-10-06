package com.example.b2b_opportunities.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "skills")
public class Skill {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Skill parent;

    @NotEmpty
    private String name;

    @NotNull
    private Boolean assignable;

    @Column(columnDefinition = "BLOB", name = "image")
    private byte[] image;

    private String imageType;

    @ManyToMany(mappedBy = "suggestedSkills")
    private List<Pattern> patterns;

    public void setName(@NotEmpty String name) {
        this.name = name.strip();
    }
}
