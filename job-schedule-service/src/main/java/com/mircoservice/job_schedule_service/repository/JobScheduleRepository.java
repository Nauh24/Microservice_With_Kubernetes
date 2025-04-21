package com.mircoservice.job_schedule_service.repository;


import com.mircoservice.job_schedule_service.model.JobSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface JobScheduleRepository extends JpaRepository<JobSchedule, Long> {
    @Query("SELECT j FROM JobSchedules j WHERE j.startingTime >= :start AND j.startingTime < :end AND j.isDeleted = false")
    List<JobSchedule> findJobsInWeek(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    Optional<JobSchedule> findByIdAndIsDeletedFalse(Long id);
//    List<JobSchedule> findByIsDeletedFalse();
    List<JobSchedule> findByIdInAndIsDeletedFalse(List<Long> jobIds);
}
