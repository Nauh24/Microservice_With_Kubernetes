package com.aad.microservice.customer_contract_service.service.impl;

import com.aad.microservice.customer_contract_service.exception.AppException;
import com.aad.microservice.customer_contract_service.exception.ErrorCode;
import com.aad.microservice.customer_contract_service.model.JobDetail;
import com.aad.microservice.customer_contract_service.model.WorkShift;
import com.aad.microservice.customer_contract_service.repository.JobDetailRepository;
import com.aad.microservice.customer_contract_service.repository.WorkShiftRepository;
import com.aad.microservice.customer_contract_service.service.WorkShiftService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Implementation of WorkShiftService
 */
@Service
public class WorkShiftServiceImpl implements WorkShiftService {
    
    private final WorkShiftRepository workShiftRepository;
    private final JobDetailRepository jobDetailRepository;
    
    // Regular expression for time format validation (HH:MM)
    private static final Pattern TIME_PATTERN = Pattern.compile("^([01]?[0-9]|2[0-3]):[0-5][0-9]$");
    
    public WorkShiftServiceImpl(WorkShiftRepository workShiftRepository, 
                               JobDetailRepository jobDetailRepository) {
        this.workShiftRepository = workShiftRepository;
        this.jobDetailRepository = jobDetailRepository;
    }
    
    @Override
    public WorkShift createWorkShift(WorkShift workShift) {
        // Validate job detail
        if (workShift.getJobDetail() == null || workShift.getJobDetail().getId() == null) {
            throw new AppException(ErrorCode.InvalidInput_Exception, "Chi tiết công việc không được để trống");
        }
        
        Optional<JobDetail> jobDetailOpt = jobDetailRepository.findByIdAndIsDeletedFalse(workShift.getJobDetail().getId());
        if (jobDetailOpt.isEmpty()) {
            throw new AppException(ErrorCode.NotFound_Exception, "Không tìm thấy thông tin chi tiết công việc");
        }
        
        // Validate time format
        validateTimeFormat(workShift.getStartTime(), "Giờ bắt đầu");
        validateTimeFormat(workShift.getEndTime(), "Giờ kết thúc");
        
        // Validate time logic
        validateTimeLogic(workShift.getStartTime(), workShift.getEndTime());
        
        // Validate number of workers
        if (workShift.getNumberOfWorkers() == null || workShift.getNumberOfWorkers() <= 0) {
            throw new AppException(ErrorCode.InvalidInput_Exception, "Số lượng nhân công phải lớn hơn 0");
        }
        
        // Set default values
        workShift.setCreatedAt(LocalDateTime.now());
        workShift.setUpdatedAt(LocalDateTime.now());
        workShift.setIsDeleted(false);
        
        // Set job detail reference
        workShift.setJobDetail(jobDetailOpt.get());
        
        return workShiftRepository.save(workShift);
    }
    
    @Override
    public WorkShift updateWorkShift(WorkShift workShift) {
        // Validate work shift exists
        Optional<WorkShift> existingWorkShift = workShiftRepository.findByIdAndIsDeletedFalse(workShift.getId());
        if (existingWorkShift.isEmpty()) {
            throw new AppException(ErrorCode.NotFound_Exception, "Không tìm thấy thông tin ca làm việc");
        }
        
        WorkShift currentWorkShift = existingWorkShift.get();
        
        // Validate time format if provided
        if (workShift.getStartTime() != null) {
            validateTimeFormat(workShift.getStartTime(), "Giờ bắt đầu");
            currentWorkShift.setStartTime(workShift.getStartTime());
        }
        
        if (workShift.getEndTime() != null) {
            validateTimeFormat(workShift.getEndTime(), "Giờ kết thúc");
            currentWorkShift.setEndTime(workShift.getEndTime());
        }
        
        // Validate time logic if both times are provided
        if (workShift.getStartTime() != null && workShift.getEndTime() != null) {
            validateTimeLogic(workShift.getStartTime(), workShift.getEndTime());
        }
        
        // Update number of workers if provided
        if (workShift.getNumberOfWorkers() != null) {
            if (workShift.getNumberOfWorkers() <= 0) {
                throw new AppException(ErrorCode.InvalidInput_Exception, "Số lượng nhân công phải lớn hơn 0");
            }
            currentWorkShift.setNumberOfWorkers(workShift.getNumberOfWorkers());
        }
        
        // Update working days if provided
        if (workShift.getWorkingDays() != null) {
            currentWorkShift.setWorkingDays(workShift.getWorkingDays());
        }
        
        // Update audit fields
        currentWorkShift.setUpdatedAt(LocalDateTime.now());
        
        return workShiftRepository.save(currentWorkShift);
    }
    
    @Override
    public void deleteWorkShift(Long id) {
        Optional<WorkShift> workShift = workShiftRepository.findByIdAndIsDeletedFalse(id);
        if (workShift.isEmpty()) {
            throw new AppException(ErrorCode.NotFound_Exception, "Không tìm thấy thông tin ca làm việc");
        }
        
        WorkShift currentWorkShift = workShift.get();
        currentWorkShift.setIsDeleted(true);
        currentWorkShift.setUpdatedAt(LocalDateTime.now());
        
        workShiftRepository.save(currentWorkShift);
    }
    
    @Override
    public WorkShift getWorkShiftById(Long id) {
        Optional<WorkShift> workShift = workShiftRepository.findByIdAndIsDeletedFalse(id);
        if (workShift.isEmpty()) {
            throw new AppException(ErrorCode.NotFound_Exception, "Không tìm thấy thông tin ca làm việc");
        }
        
        return workShift.get();
    }
    
    @Override
    public List<WorkShift> getAllWorkShifts() {
        return workShiftRepository.findByIsDeletedFalse();
    }
    
    @Override
    public List<WorkShift> getWorkShiftsByJobDetailId(Long jobDetailId) {
        return workShiftRepository.findByJobDetail_IdAndIsDeletedFalse(jobDetailId);
    }
    
    @Override
    public boolean checkWorkShiftExists(Long id) {
        return workShiftRepository.findByIdAndIsDeletedFalse(id).isPresent();
    }
    
    /**
     * Validate time format (HH:MM)
     * @param time Time string to validate
     * @param fieldName Field name for error message
     */
    private void validateTimeFormat(String time, String fieldName) {
        if (time == null || time.isEmpty()) {
            throw new AppException(ErrorCode.InvalidInput_Exception, fieldName + " không được để trống");
        }
        
        if (!TIME_PATTERN.matcher(time).matches()) {
            throw new AppException(ErrorCode.InvalidInput_Exception, 
                fieldName + " phải có định dạng HH:MM (ví dụ: 08:30)");
        }
    }
    
    /**
     * Validate time logic (start time must be before end time)
     * @param startTime Start time string
     * @param endTime End time string
     */
    private void validateTimeLogic(String startTime, String endTime) {
        // Parse times to compare
        String[] startParts = startTime.split(":");
        String[] endParts = endTime.split(":");
        
        int startHour = Integer.parseInt(startParts[0]);
        int startMinute = Integer.parseInt(startParts[1]);
        
        int endHour = Integer.parseInt(endParts[0]);
        int endMinute = Integer.parseInt(endParts[1]);
        
        // Compare times
        if (startHour > endHour || (startHour == endHour && startMinute >= endMinute)) {
            throw new AppException(ErrorCode.InvalidInput_Exception, 
                "Giờ bắt đầu phải trước giờ kết thúc");
        }
    }
}
