package com.example.mircroservice.register_service.client;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "job-schedule-service", url = "http://localhost:8081/api/job-schedule")
public interface IJobScheduleClient {

    @RequestMapping(method = RequestMethod.GET, value = "/{id}/check-job-exists")
    Boolean CheckJobExists(@PathVariable long id);

    @RequestMapping(method = RequestMethod.GET, value = "/{id}/check-overlap-with-job-ids")
    Boolean checkOverlapWithJobIds(@PathVariable long id, @RequestParam("jobIds") List<Long> jobIds);

//    @RequestMapping(method = RequestMethod.GET, value = "api/job/{id}")
//    SimpleJobDto GetJobById(@PathVariable long id);
}
