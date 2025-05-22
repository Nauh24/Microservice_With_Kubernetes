package com.aad.microservice.customer_contract_service.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JobDetail Entity
 * Represents a specific job type within a customer contract
 */
@Entity
@Table(name = "job_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relationship with CustomerContract
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    @JsonBackReference
    private CustomerContract contract;

    // Job Category information (service-to-service communication)
    private Long jobCategoryId;

    // Job specific information
    private LocalDate startDate;
    private LocalDate endDate;
    private String workLocation;

    // Relationship with WorkShift
    @OneToMany(mappedBy = "jobDetail", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkShift> workShifts = new ArrayList<>();

    // Audit fields
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Add a work shift to this job detail
     * @param workShift The work shift to add
     */
    public void addWorkShift(WorkShift workShift) {
        workShifts.add(workShift);
        workShift.setJobDetail(this);
    }

    /**
     * Remove a work shift from this job detail
     * @param workShift The work shift to remove
     */
    public void removeWorkShift(WorkShift workShift) {
        workShifts.remove(workShift);
        workShift.setJobDetail(null);
    }
}
