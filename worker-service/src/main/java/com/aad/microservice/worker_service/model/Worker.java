package com.aad.microservice.worker_service.model;

import com.aad.microservice.worker_service.constant.Gender;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "Workers")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Worker {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String fullName;
    private String phoneNumber;

    private String email;

    private Boolean isDeleted;

    private String identityCardNo; // Căn cước công dân

    private String workingExperienceAndSkill;
    private LocalDate dob;
    private String address;
    private Gender gender;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
