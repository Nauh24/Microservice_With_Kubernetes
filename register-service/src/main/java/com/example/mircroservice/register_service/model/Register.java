package com.example.mircroservice.register_service.model;

import com.example.mircroservice.register_service.constant.RegisterStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity(name = "Registers")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Register {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private long workerId;
    private long jobId;

    private RegisterStatus status;
    private boolean isDeleted;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
