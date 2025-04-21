package com.mircoservice.job_schedule_service.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "work-schedule-service", url = "http://localhost:8082")
public interface IRegisterClient {
//    @RequestMapping(method = RequestMethod.GET, value = "/api/work-schedule/get-verified-work-schedule-by-job-id/{jobId}")
//    List<WorkScheduleDto> getVerifiedWorkSchedulesByJobId(@PathVariable long jobId);
}
