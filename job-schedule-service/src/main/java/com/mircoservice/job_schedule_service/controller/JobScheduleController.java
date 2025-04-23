package com.mircoservice.job_schedule_service.controller;

import com.mircoservice.job_schedule_service.model.JobSchedule;
import com.mircoservice.job_schedule_service.service.JobServiceImpl;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/job-schedule")
public class JobScheduleController {
    private JobServiceImpl _jobServiceImpl;

    public JobScheduleController(JobServiceImpl jobServiceImpl){
        _jobServiceImpl = jobServiceImpl;
    }
    // Lấy danh sách công việc trong tuần
    @GetMapping("/weekly")
    public ResponseEntity<List<JobSchedule>> getWeeklyJobs(@RequestParam LocalDate dateInWeek) {
        List<JobSchedule> response = _jobServiceImpl.getJobsForWeek(dateInWeek);
        return ResponseEntity.ok(response);
    }
//    @GetMapping("/{id}")
//    public ResponseEntity<JobSchedule> getJobById(@PathVariable long id){
//        return ResponseEntity.ok(_jobServiceImpl.getJobById(id));
//    }
    // Internal API
    @GetMapping("/{id}/check-job-exists")
    public ResponseEntity<Boolean> checkJobExists(@PathVariable long id){
        boolean canRegister = _jobServiceImpl.checkJobExists(id);
        return ResponseEntity.ok(canRegister);
    }
    @GetMapping("/{id}/check-overlap-with-job-ids")
    public ResponseEntity<Boolean> checkOverlapWithJobIds(@PathVariable long id, @RequestParam("jobIds") List<Long> jobIds){
        boolean isOverLap = _jobServiceImpl.checkOverlapWithJobIds(id, jobIds);
        return ResponseEntity.ok(isOverLap);
    }
}
