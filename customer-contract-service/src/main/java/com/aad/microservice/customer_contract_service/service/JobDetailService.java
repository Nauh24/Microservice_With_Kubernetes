package com.aad.microservice.customer_contract_service.service;

import com.aad.microservice.customer_contract_service.model.JobDetail;

import java.util.List;

/**
 * Service interface for JobDetail entity
 */
public interface JobDetailService {

    JobDetail createJobDetail(JobDetail jobDetail);

    JobDetail updateJobDetail(JobDetail jobDetail);

    void deleteJobDetail(Long id);

    JobDetail getJobDetailById(Long id);

    List<JobDetail> getAllJobDetails();

    List<JobDetail> getJobDetailsByContractId(Long contractId);

    List<JobDetail> getJobDetailsByJobCategoryId(Long jobCategoryId);

    boolean checkJobDetailExists(Long id);
}
