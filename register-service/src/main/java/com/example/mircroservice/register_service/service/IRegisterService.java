package com.example.mircroservice.register_service.service;

import com.example.mircroservice.register_service.constant.RegisterStatus;
import com.example.mircroservice.register_service.model.Register;

import java.util.List;

public interface IRegisterService {
    Register create(Register schedule);
    Register updateStatus(Long id, RegisterStatus status);
    List<Register> getRegistersByWorker(Long workerId);
    void deleteRegister(Long id);
}
