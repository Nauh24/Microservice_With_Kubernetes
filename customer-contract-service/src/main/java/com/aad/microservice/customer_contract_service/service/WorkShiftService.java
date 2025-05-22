package com.aad.microservice.customer_contract_service.service;

import com.aad.microservice.customer_contract_service.model.WorkShift;

import java.util.List;

/**
 * Service interface for WorkShift entity
 */
public interface WorkShiftService {
    
    /**
     * Create a new work shift
     * @param workShift Work shift to create
     * @return Created work shift
     */
    WorkShift createWorkShift(WorkShift workShift);
    
    /**
     * Update an existing work shift
     * @param workShift Work shift to update
     * @return Updated work shift
     */
    WorkShift updateWorkShift(WorkShift workShift);
    
    /**
     * Delete a work shift
     * @param id Work shift ID
     */
    void deleteWorkShift(Long id);
    
    /**
     * Get a work shift by ID
     * @param id Work shift ID
     * @return Work shift
     */
    WorkShift getWorkShiftById(Long id);
    
    /**
     * Get all work shifts
     * @return List of work shifts
     */
    List<WorkShift> getAllWorkShifts();
    
    /**
     * Get all work shifts for a job detail
     * @param jobDetailId Job detail ID
     * @return List of work shifts
     */
    List<WorkShift> getWorkShiftsByJobDetailId(Long jobDetailId);
    
    /**
     * Check if a work shift exists
     * @param id Work shift ID
     * @return True if the work shift exists, false otherwise
     */
    boolean checkWorkShiftExists(Long id);
}
