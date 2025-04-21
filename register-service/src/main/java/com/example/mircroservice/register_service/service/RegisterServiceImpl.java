package com.example.mircroservice.register_service.service;

import com.example.mircroservice.register_service.client.IJobScheduleClient;
import com.example.mircroservice.register_service.client.IWorkerClient;
import com.example.mircroservice.register_service.client.IWorkerContractClient;
import com.example.mircroservice.register_service.constant.RegisterStatus;
import com.example.mircroservice.register_service.exception.AppException;
import com.example.mircroservice.register_service.exception.ErrorCode;
import com.example.mircroservice.register_service.model.Register;
import org.springframework.stereotype.Service;
import com.example.mircroservice.register_service.repository.IRegisterRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RegisterServiceImpl implements IRegisterService {
    private final IRegisterRepository _IRegisterRepository;
    private final IJobScheduleClient _jobClient;
    private final IWorkerClient _workerClient;
    private final IWorkerContractClient _workerContractClient;

    public RegisterServiceImpl(IRegisterRepository IRegisterRepository, IJobScheduleClient jobClient, IWorkerClient workerClient, IWorkerContractClient workerContractClient) {
        _IRegisterRepository = IRegisterRepository;
        _jobClient = jobClient;
        _workerClient = workerClient;
        _workerContractClient = workerContractClient;
    }

//    public List<WorkSchedule> getVerifiedSchedulesByJobId(Long jobId) {
//        return _workScheduleRepository.findByJobIdAndStatus(jobId, WorkScheduleStatus.Approved);
//    }
//
//    public Boolean createWorkSchedule(CreateWorkScheduleCommand command){
//        boolean canRegisterJob = _jobClient.CheckCanRegister(command.jobId);
//        boolean isValidWorker = _workerClient.CheckIsValidWorker(command.workerId);
//        WorkSchedule existedWorkSchedule = _workScheduleRepository.FindWorkScheduleByJobIdAndWorkerId(command.jobId, command.workerId);
//        if(!canRegisterJob || !isValidWorker || existedWorkSchedule != null){
//            return false;
//        }
//
//        WorkSchedule workSchedule = new WorkSchedule();
////        workSchedule.setStatus(Constant.WorkScheduleStatus.AwaitingVerified);
//        workSchedule.setStatus(WorkScheduleStatus.Approved);
//        workSchedule.setJobId(command.jobId);
//        workSchedule.setWorkerId(command.workerId);
//
//        _workScheduleRepository.save(workSchedule);
//        return true;
//    }
//
//    public List<WorkScheduleDetailDto> GetAllWorkSchedules(){
//        List<WorkSchedule> workSchedules = _workScheduleRepository.findAll();
//        List<WorkScheduleDetailDto> res = new ArrayList<>();
//        for (WorkSchedule ws : workSchedules) {
//            WorkScheduleDetailDto dto = new WorkScheduleDetailDto();
//            dto.setId(ws.getId());
//            dto.setJobId(ws.getJobId());
//            dto.setWorkerId(ws.getWorkerId());
//            dto.setStatus(ws.getStatus());
//
//            SimpleJobDto job = _jobClient.GetJobById(ws.getJobId());
//            SimpleWorkerDto worker = _workerClient.GetWorkerById(ws.getWorkerId());
//
//            dto.setJob(job);
//            dto.setWorker(worker);
//
//            res.add(dto);
//        }
//        return res;
//    }

    @Override
    public Register create(Register register) {
        // Kiểm tra workerId có tồn tại không
        boolean isExistedWorker = _workerClient.CheckWorkerExists(register.getWorkerId());
        if(!isExistedWorker){
            throw new AppException(ErrorCode.NotFound_Exception, "Thông tin nhân công không tồn tại");
        }
        // Kiểm tra jobId có tồn tại không
        boolean isExistedJob = _jobClient.CheckJobExists(register.getJobId());
        if(!isExistedJob){
            throw new AppException(ErrorCode.NotFound_Exception, "Thông tin công việc không tồn tại");
        }
        // Kiểm tra nhân công có bị trùng lịch không
            // Lấy ds đăng ký của công nhân có trạng thái = Awaiting_Approved | Approved
            // Gọi sang JobService để check trùng lịch
        List<RegisterStatus> statuses = List.of(
                RegisterStatus.AwaitingApproved,
                RegisterStatus.Approved
        );
        List<Register> registeredRegisters = _IRegisterRepository.findByWorkerIdAndIsDeletedFalseAndStatusIn(register.getWorkerId(), statuses);
        List<Long> jobIds = registeredRegisters.stream()
                .map(Register::getJobId)
                .toList();

        boolean isOverlapped = _jobClient.checkOverlapWithJobIds(register.getJobId(), jobIds);
        if(isOverlapped){
            throw new AppException(ErrorCode.NotAllowCreate_Exception, "Bạn đã có lịch làm việc trùng với công việc này");
        }

        // Nhân công phải có hợp đồng lao động đang có hiệu lực trong ngày diễn ra công việc không
        boolean existActiveWorkerContract = _workerContractClient.CheckExistsActiveWorkerContractByWorkerId(register.getWorkerId());
        if(!existActiveWorkerContract){
            throw new AppException(ErrorCode.NotAllowCreate_Exception, "Nhân công không có hợp đồng lao động đang có hiệu lực");
        }

        // Kiểm tra nhân công đã đăng ký công việc này chưa
        boolean alreadyRegisteredJob = _IRegisterRepository.existsByWorkerIdAndJobIdAndIsDeletedFalse(register.getWorkerId(), register.getJobId());
        if(alreadyRegisteredJob){
            throw new AppException(ErrorCode.NotAllowCreate_Exception, "Bạn đã đăng ký công việc này rồi");
        }

        register.setCreatedAt(LocalDateTime.now());
        register.setUpdatedAt(LocalDateTime.now());
        register.setDeleted(false);
        register.setStatus(RegisterStatus.AwaitingApproved);
        return _IRegisterRepository.save(register);
    }

    @Override
    public Register updateStatus(Long id, RegisterStatus status) {
        Register schedule = _IRegisterRepository.findById(id).orElseThrow(() -> new RuntimeException("Schedule not found"));
        schedule.setStatus(status);
        schedule.setUpdatedAt(LocalDateTime.now());
        return _IRegisterRepository.save(schedule);
    }

    @Override
    public List<Register> getRegistersByWorker(Long workerId) {
        return _IRegisterRepository.findByWorkerIdAndIsDeletedFalse(workerId);
    }

    @Override
    public void deleteRegister(Long id) {
        Register register = _IRegisterRepository.findById(id).orElseThrow(() -> new RuntimeException("Schedule not found"));
        register.setDeleted(true);
        register.setUpdatedAt(LocalDateTime.now());
        _IRegisterRepository.save(register);
    }
}
