package com.example.b2b_opportunities.Entity;

import com.example.b2b_opportunities.Static.JobType;
import com.example.b2b_opportunities.Static.WorkLocation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "job_listings") // TODO
public class JobListing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    private String title;

    private String location;

    private int rate_from; // set both the same value to have just 1 price visible
    private int rate_to;

    @Enumerated(EnumType.STRING) // Use EnumType.ORDINAL for ordinal values
    private JobType jobType;

    @Enumerated(EnumType.STRING) // Use EnumType.ORDINAL for ordinal values
    private List<WorkLocation> workLocation;

    private boolean isActive;

    @ManyToMany
    @JoinTable(
            name = "job_listing_optional_tech_stack",
            joinColumns = @JoinColumn(name = "job_listing_id"),
            inverseJoinColumns = @JoinColumn(name = "tech_stack_id")
    )
    private Set<TechStack> requiredTechStack;

    @ManyToMany
    @JoinTable(
            name = "job_listing_optional_tech_stack",
            joinColumns = @JoinColumn(name = "job_listing_id"),
            inverseJoinColumns = @JoinColumn(name = "tech_stack_id")
    )
    private Set<TechStack> optionalTechStack; // TODO: if TeckStack uses Enum for Tech - this can be just the Tech Enum.

    // TODO - receive notifications - to be implemented (List<Users>?

    private String overview;
    private Set<String> responsibilities; // TODO: String or Enum?
    private Set<String> hiringProcess; // TODO: String or Enum?

    @ManyToMany(mappedBy = "jobListings")
    private List<Project> projects;


}
