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
    
    @GetMapping("/{id}")
    public ResponseEntity<WorkShift> getWorkShiftById(@PathVariable Long id) {
        WorkShift workShift = workShiftService.getWorkShiftById(id);
        return ResponseEntity.ok(workShift);
    }
    
    @PostMapping
    public ResponseEntity<WorkShift> createWorkShift(@RequestBody WorkShift workShift) {
        return ResponseEntity.ok(workShiftService.createWorkShift(workShift));
    }
    
    @GetMapping
    public ResponseEntity<List<WorkShift>> getAllWorkShifts() {
        return ResponseEntity.ok(workShiftService.getAllWorkShifts());
    }
    
    @PutMapping
    public ResponseEntity<WorkShift> updateWorkShift(@RequestBody WorkShift workShift) {
        return ResponseEntity.ok(workShiftService.updateWorkShift(workShift));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkShift(@PathVariable Long id) {
        workShiftService.deleteWorkShift(id);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/job-detail/{jobDetailId}")
    public ResponseEntity<List<WorkShift>> getWorkShiftsByJobDetailId(@PathVariable Long jobDetailId) {
        return ResponseEntity.ok(workShiftService.getWorkShiftsByJobDetailId(jobDetailId));
    }

    @GetMapping("/{id}/check-work-shift-exists")
    public ResponseEntity<Boolean> checkWorkShiftExists(@PathVariable Long id) {
        boolean exists = workShiftService.checkWorkShiftExists(id);
        return ResponseEntity.ok(exists);
    }
}
