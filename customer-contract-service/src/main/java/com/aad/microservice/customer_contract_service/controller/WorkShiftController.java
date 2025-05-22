package com.aad.microservice.customer_contract_service.controller;

import com.aad.microservice.customer_contract_service.model.WorkShift;
import com.aad.microservice.customer_contract_service.service.WorkShiftService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for WorkShift entity
 */
@RestController
@RequestMapping("/api/work-shift")
public class WorkShiftController {
    
    private final WorkShiftService workShiftService;
    
    public WorkShiftController(WorkShiftService workShiftService) {
        this.workShiftService = workShiftService;
    }
    
    /**
     * Get a work shift by ID
     * @param id Work shift ID
     * @return ResponseEntity containing the work shift
     */
    @GetMapping("/{id}")
    public ResponseEntity<WorkShift> getWorkShiftById(@PathVariable Long id) {
        WorkShift workShift = workShiftService.getWorkShiftById(id);
        return ResponseEntity.ok(workShift);
    }
    
    /**
     * Create a new work shift
     * @param workShift Work shift to create
     * @return ResponseEntity containing the created work shift
     */
    @PostMapping
    public ResponseEntity<WorkShift> createWorkShift(@RequestBody WorkShift workShift) {
        return ResponseEntity.ok(workShiftService.createWorkShift(workShift));
    }
    
    /**
     * Get all work shifts
     * @return ResponseEntity containing the list of work shifts
     */
    @GetMapping
    public ResponseEntity<List<WorkShift>> getAllWorkShifts() {
        return ResponseEntity.ok(workShiftService.getAllWorkShifts());
    }
    
    /**
     * Update a work shift
     * @param workShift Work shift to update
     * @return ResponseEntity containing the updated work shift
     */
    @PutMapping
    public ResponseEntity<WorkShift> updateWorkShift(@RequestBody WorkShift workShift) {
        return ResponseEntity.ok(workShiftService.updateWorkShift(workShift));
    }
    
    /**
     * Delete a work shift
     * @param id Work shift ID
     * @return ResponseEntity with no content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkShift(@PathVariable Long id) {
        workShiftService.deleteWorkShift(id);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Get all work shifts for a job detail
     * @param jobDetailId Job detail ID
     * @return ResponseEntity containing the list of work shifts
     */
    @GetMapping("/job-detail/{jobDetailId}")
    public ResponseEntity<List<WorkShift>> getWorkShiftsByJobDetailId(@PathVariable Long jobDetailId) {
        return ResponseEntity.ok(workShiftService.getWorkShiftsByJobDetailId(jobDetailId));
    }
    
    /**
     * Check if a work shift exists
     * @param id Work shift ID
     * @return ResponseEntity containing true if the work shift exists, false otherwise
     */
    @GetMapping("/{id}/check-work-shift-exists")
    public ResponseEntity<Boolean> checkWorkShiftExists(@PathVariable Long id) {
        boolean exists = workShiftService.checkWorkShiftExists(id);
        return ResponseEntity.ok(exists);
    }
}
