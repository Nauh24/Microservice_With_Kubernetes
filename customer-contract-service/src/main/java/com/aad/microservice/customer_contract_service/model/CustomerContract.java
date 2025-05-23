package com.aad.microservice.customer_contract_service.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    private Double totalAmount;
    private Double totalPaid;
    private String address;
    private String description;

    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @Builder.Default
    private List<JobDetail> jobDetails = new ArrayList<>();

    private Integer status;          // Trạng thái hợp đồng (0: Chờ xử lý, 1: Đang hoạt động, 2: Hoàn thành, 3: Đã hủy)
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Long customerId;

    public void addJobDetail(JobDetail jobDetail) {
        jobDetails.add(jobDetail);
        jobDetail.setContract(this);
    }

    public void removeJobDetail(JobDetail jobDetail) {
        jobDetails.remove(jobDetail);
        jobDetail.setContract(null);
    }
}
