package com.aad.microservice.customer_contract_service.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    @JsonBackReference
    private CustomerContract contract;

    private Long jobCategoryId;

    private LocalDate startDate;
    private LocalDate endDate;
    private String workLocation;

    @OneToMany(mappedBy = "jobDetail", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<WorkShift> workShifts = new ArrayList<>();

    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public void addWorkShift(WorkShift workShift) {
        workShifts.add(workShift);
        workShift.setJobDetail(this);
    }

    public void removeWorkShift(WorkShift workShift) {
        workShifts.remove(workShift);
        workShift.setJobDetail(null);
    }
}
