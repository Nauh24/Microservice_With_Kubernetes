package com.mircoservice.job_schedule_service.service;

import com.mircoservice.job_schedule_service.model.JobSchedule;

import java.time.LocalDate;
import java.util.List;

public interface IJobService {
    JobSchedule getJobById(Long id);
//    List<JobSchedule> getAllJobs();
    boolean checkOverlapWithJobIds(Long jobId, List<Long> jobIds);
    boolean checkJobExists(Long id);
    List<JobSchedule> getJobsForWeek(LocalDate dateInWeek);
}
