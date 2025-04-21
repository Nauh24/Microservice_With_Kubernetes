package com.aad.microservice.worker_contract_service.model;

import com.aad.microservice.worker_contract_service.constant.WorkerContractStatus;
import com.aad.microservice.worker_contract_service.externalModel.Worker;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "WorkerContracts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkerContract {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String contractCode;

    private Long workerId;

    private String contractTitle;
    private LocalDate startDate; // Ngày bắt đầu hợp đồng
    private LocalDate endDate; // Ngày kết thúc hợp đồng
    private WorkerContractStatus status;
    private Double salary;
    private Boolean isDeleted;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Transient
    private Worker worker;
}

