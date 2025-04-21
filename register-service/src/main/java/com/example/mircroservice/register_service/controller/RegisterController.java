package com.example.mircroservice.register_service.controller;

import com.example.mircroservice.register_service.model.Register;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.mircroservice.register_service.service.RegisterServiceImpl;

@RestController
@RequestMapping("/api/register")
public class RegisterController {
    private RegisterServiceImpl registerService;

    public RegisterController(RegisterServiceImpl workScheduleServiceImpl) {
        registerService = workScheduleServiceImpl;
    }
//    @GetMapping("/get-verified-work-schedule-by-job-id/{jobId}")
//    public ResponseEntity<List<WorkSchedule>> getVerifiedWorkSchedulesByJobId(@PathVariable long jobId) {
//        List<WorkSchedule> res = _workScheduleServiceImpl.getVerifiedSchedulesByJobId(jobId);
//        return ResponseEntity.ok(res);
//    }
    @PostMapping("")
    public ResponseEntity<Register> createWorkSchedule(@RequestBody Register register) {
        return ResponseEntity.ok(registerService.create(register));
    }

//    @GetMapping("/all")
//    public ResponseEntity<List<WorkScheduleDetailDto>> GetAllWorkSchedules(){
//        return ResponseEntity.ok(registerService.GetAllWorkSchedules());
//    }
}
