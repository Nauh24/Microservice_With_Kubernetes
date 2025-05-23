package com.aad.microservice.customer_contract_service.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * CustomerContract Entity
 * Represents a contract between a customer and the company
 */
@Entity
@Table(name = "customer_contracts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerContract {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate startingDate;
    private LocalDate endingDate;
    private LocalDate signedDate;

    // Thông tin công việc
    private Double totalAmount;
    private Double totalPaid;
    private String address;
    private String description;

    private Long customerId;

    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @Builder.Default
    private List<JobDetail> jobDetails = new ArrayList<>();

    // Trạng thái và thông tin hệ thống
    private Integer status;          // Trạng thái hợp đồng (0: Chờ xử lý, 1: Đang hoạt động, 2: Hoàn thành, 3: Đã hủy)
    private Boolean isDeleted;     
    private LocalDateTime createdAt; 
    private LocalDateTime updatedAt; 

    /**
     * Add a job detail to this contract
     * @param jobDetail The job detail to add
     */
    public void addJobDetail(JobDetail jobDetail) {
        jobDetails.add(jobDetail);
        jobDetail.setContract(this);
    }

    /**
     * Remove a job detail from this contract
     * @param jobDetail The job detail to remove
     */
    public void removeJobDetail(JobDetail jobDetail) {
        jobDetails.remove(jobDetail);
        jobDetail.setContract(null);
    }

    /**
     * Calculate the total number of workers across all job details
     * @return The total number of workers
     */
    @Transient
    public Integer getTotalNumberOfWorkers() {
        if (jobDetails == null || jobDetails.isEmpty()) {
            return 0;
        }

        return jobDetails.stream()
            .flatMap(jd -> jd.getWorkShifts().stream())
            .mapToInt(WorkShift::getNumberOfWorkers)
            .sum();
    }
}
