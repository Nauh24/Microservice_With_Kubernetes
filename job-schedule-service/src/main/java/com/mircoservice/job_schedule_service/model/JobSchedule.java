package com.mircoservice.job_schedule_service.model;

import com.mircoservice.job_schedule_service.constant.JobScheduleStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity(name = "JobSchedules")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String title; // TODO: Replace this field -> JobType
    private String description;
    private String address;
    private LocalDateTime startingTime;
    private LocalDateTime endingTime;
    private double salaryPerWorker;
    private int maxWorker;

    private JobScheduleStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private boolean isDeleted;
}
