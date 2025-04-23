package com.mircoservice.job_schedule_service.service;

import com.mircoservice.job_schedule_service.client.IRegisterClient;
import com.mircoservice.job_schedule_service.exception.AppException;
import com.mircoservice.job_schedule_service.exception.ErrorCode;
import com.mircoservice.job_schedule_service.model.JobSchedule;
import com.mircoservice.job_schedule_service.repository.JobScheduleRepository;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class JobServiceImpl implements IJobService{
    private final JobScheduleRepository _jobScheduleRepository;
    // Biến này sẽ được sử dụng trong tương lai
    @SuppressWarnings("unused")
    private final IRegisterClient _workScheduleClient;
    public JobServiceImpl(JobScheduleRepository jobScheduleRepository, IRegisterClient IRegisterClient){
        _jobScheduleRepository = jobScheduleRepository;
        _workScheduleClient = IRegisterClient;
    }

    public List<JobSchedule> getJobsForWeek(LocalDate dateInWeek){
        // Tính ngày bắt đầu và kết thúc của tuần
        LocalDate weekStart = dateInWeek.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = weekStart.plusDays(6); // Chủ nhật

        // Tìm các công việc trong tuần
        LocalDateTime weekStartTime = weekStart.atStartOfDay();
        LocalDateTime weekEndTime = weekEnd.plusDays(1).atStartOfDay();

        List<JobSchedule> jobs = _jobScheduleRepository.findJobsInWeek(weekStartTime, weekEndTime);

        return jobs;
    }



    @Override
    public JobSchedule getJobById(Long id) {
        Optional<JobSchedule> job = _jobScheduleRepository.findByIdAndIsDeletedFalse(id);
        if(job.isEmpty()) {
            throw new AppException(ErrorCode.NotFound_Exception, "Không tìm thấy công việc");
        }
        return job.get();
    }

//    @Override
//    public List<JobSchedule> getAllJobs() {
//        List<JobSchedule> JobSchedules = _jobScheduleRepository.findByIsDeletedFalse();
//        return JobSchedules;
//    }

    @Override
    public boolean checkOverlapWithJobIds(Long jobId, List<Long> jobIds) {
        JobSchedule baseJobSchedule = getJobById(jobId);
        List<JobSchedule> comparedJobSchedules = _jobScheduleRepository.findByIdInAndIsDeletedFalse(jobIds);
        for (JobSchedule comparedJobSchedule : comparedJobSchedules) {
            if (comparedJobSchedule.getId() == baseJobSchedule.getId()) {
                continue;
            }
            // Overlap when baseStartTime < comparedEndTime and baseEndTime > comparedStartTime
            // 3 cases
            if (baseJobSchedule.getStartingTime().isBefore(comparedJobSchedule.getEndingTime()) &&
                    baseJobSchedule.getEndingTime().isAfter(comparedJobSchedule.getStartingTime())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean checkJobExists(Long id) {
        Optional<JobSchedule> job = _jobScheduleRepository.findByIdAndIsDeletedFalse(id);
        return job.isPresent();
    }
}
