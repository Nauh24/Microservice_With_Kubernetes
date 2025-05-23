package com.aad.microservice.customer_contract_service.service;

import com.aad.microservice.customer_contract_service.model.WorkShift;

import java.util.List;

/**
 * Service interface for WorkShift entity
 */
public interface WorkShiftService {

    WorkShift createWorkShift(WorkShift workShift);

    WorkShift updateWorkShift(WorkShift workShift);

    void deleteWorkShift(Long id);

    WorkShift getWorkShiftById(Long id);

    List<WorkShift> getAllWorkShifts();

    List<WorkShift> getWorkShiftsByJobDetailId(Long jobDetailId);

    boolean checkWorkShiftExists(Long id);
}
