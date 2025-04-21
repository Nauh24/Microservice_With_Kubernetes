package com.aad.microservice.worker_contract_service.externalModel;

import com.aad.microservice.worker_contract_service.constant.Gender;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class Worker {
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
