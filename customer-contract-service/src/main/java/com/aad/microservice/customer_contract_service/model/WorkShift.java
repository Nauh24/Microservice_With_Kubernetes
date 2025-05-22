package com.aad.microservice.customer_contract_service.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * WorkShift Entity
 * Represents a specific work shift within a job detail
 */
@Entity
@Table(name = "work_shifts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkShift {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relationship with JobDetail
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_detail_id", nullable = false)
    @JsonBackReference
    private JobDetail jobDetail;

    // Work shift information
    private String startTime;  // Format: HH:MM
    private String endTime;    // Format: HH:MM
    private Integer numberOfWorkers;

    // Working days (stored as JSON array of day numbers)
    // 1: Monday, 2: Tuesday, 3: Wednesday, 4: Thursday, 5: Friday, 6: Saturday, 7: Sunday
    @Column(columnDefinition = "TEXT")
    private String workingDays;

    // Audit fields
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
