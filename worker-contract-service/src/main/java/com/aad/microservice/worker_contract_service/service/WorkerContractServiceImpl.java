package com.aad.microservice.worker_contract_service.service;

import com.aad.microservice.worker_contract_service.client.IWorkerClient;
import com.aad.microservice.worker_contract_service.constant.WorkerContractStatus;
import com.aad.microservice.worker_contract_service.exception.AppException;
import com.aad.microservice.worker_contract_service.exception.ErrorCode;
import com.aad.microservice.worker_contract_service.model.WorkerContract;
import com.aad.microservice.worker_contract_service.repository.WorkerContractRepository;
import org.hibernate.jdbc.Work;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class WorkerContractServiceImpl implements IWorkerContractService {
    private final WorkerContractRepository _workerContractRepository;
    private final IWorkerClient _workerClient;
    public WorkerContractServiceImpl(WorkerContractRepository repository, IWorkerClient workerClient){
        _workerContractRepository = repository;
        _workerClient = workerClient;
    }

    @Override
    public WorkerContract createContract(WorkerContract contract) {
        validateCreateContract(contract);

        contract.setCreatedAt(LocalDateTime.now());
        contract.setUpdatedAt(LocalDateTime.now());
        contract.setIsDeleted(false);
        // Cập nhật status của hợp đồng
        LocalDate currentDate = LocalDate.now();

        contract.setStatus(GetContractStatusBasedOnCurrentDate(contract));

        WorkerContract saved = _workerContractRepository.save(contract);
        saved.setContractCode("HD" + saved.getId());

        return _workerContractRepository.save(saved);
    }

    @Override
    public WorkerContract updateContract(WorkerContract contract) {
        WorkerContract existing = getContractById(contract.getId());

        // current date >= startDate --> Not allow update
        if (existing.getStartDate().isBefore(LocalDate.now()) || existing.getStartDate().isEqual(LocalDate.now())) {
            throw new AppException(ErrorCode.NotAllowUpdate_Exception, "Không thể cập nhật hợp đồng đã có hiệu lực hoặc hết hạn");
        }

        existing.setContractTitle(contract.getContractTitle());
        existing.setStartDate(contract.getStartDate());
        existing.setEndDate(contract.getEndDate());
        existing.setSalary(contract.getSalary());
        existing.setUpdatedAt(LocalDateTime.now());

        validateCreateContract(existing);

        existing.setStatus(GetContractStatusBasedOnCurrentDate(existing));

        return _workerContractRepository.save(existing);
    }

    @Override
    public void deleteContract(Long id) {
        WorkerContract contract = getContractById(id);

        if(contract.getStartDate().isBefore(LocalDate.now()) || contract.getStartDate().isEqual(LocalDate.now())) {
            throw new AppException(ErrorCode.NotAllowUpdate_Exception, "Chỉ được xóa hợp đồng ở trạng thái chưa kích hoạt");
        }

        contract.setIsDeleted(true);
        _workerContractRepository.save(contract);
    }

    @Override
    public WorkerContract getContractById(Long id) {
        Optional<WorkerContract> contract = _workerContractRepository.findById(id);
        if(contract.isEmpty() || contract.get().getIsDeleted()){
            throw new AppException(ErrorCode.NotFound_Exception, "Không tìm thấy hợp đồng");
        }

        contract.get().setStatus(GetContractStatusBasedOnCurrentDate(contract.get()));

        return _workerContractRepository.save(contract.get());
    }

//    @Override
//    public List<WorkerContract> getContractsByWorkerId(Long workerId) {
//        List<WorkerContract> workerContracts = _workerContractRepository.findByWorkerIdAndIsDeletedFalse(workerId);
//        for(WorkerContract contract : workerContracts){
//            contract.setStatus(GetContractStatusBasedOnCurrentDate(contract));
//            _workerContractRepository.save(contract);
//        }
//        return workerContracts;
//    }

    @Override
    public List<WorkerContract> getAllContracts() {
        List<WorkerContract> workerContracts = _workerContractRepository.findByIsDeletedFalse();
        for(WorkerContract contract : workerContracts){
            contract.setStatus(GetContractStatusBasedOnCurrentDate(contract));
            _workerContractRepository.save(contract);

            contract.setWorker(_workerClient.GetWorkerById(contract.getWorkerId()));
        }
        return workerContracts;
    }


    public void validateCreateContract(WorkerContract contract){
        if(!_workerClient.CheckWorkerExists(contract.getWorkerId())){
            throw new AppException(ErrorCode.NotAllowCreate_Exception, "Nhân công không tồn tại");
        }

        if(!_workerContractRepository.findConflictingContracts(contract.getWorkerId(), contract.getStartDate(), contract.getEndDate()).isEmpty()){
            throw new AppException(ErrorCode.NotAllowCreate_Exception, "Nhân công đã có hợp đồng lao động trong thời gian gian từ "
                    + contract.getStartDate() + " đến " + contract.getEndDate());
        }

        if (contract.getStartDate().isAfter(contract.getEndDate())) {
            throw new AppException(ErrorCode.NotAllowCreate_Exception, "Ngày bắt đầu không được sau ngày kết thúc");
        }

        if (contract.getSalary() == null || contract.getSalary() < 0) {
            throw new AppException(ErrorCode.NotAllowCreate_Exception, "Mức lương phải lớn hơn hoặc bằng 0");
        }

        if (contract.getStartDate().isBefore(LocalDate.now())) {
            throw new AppException(ErrorCode.NotAllowCreate_Exception, "Ngày bắt đầu hợp đồng phải >= ngày hiện tại");
        }
    }
// Nếu có chức năng chấm dứt hợp đồng sớm thì cần gọi sang register service để check xem tại thời điểm chấm dứt đang có cv nào k
//    @Override
//    public WorkerContract terminateContractEarly(Long id) {
//        WorkerContract contract = getContractById(id);
//
//        LocalDate currentDate = LocalDate.now();
//
//        // currentDate < startDate || currentDate >= endDate
//        if (currentDate.isBefore(contract.getStartDate()) && !currentDate.isBefore(contract.getEndDate())) {
//            throw new AppException(ErrorCode.NotAllowUpdate_Exception, "Chỉ được kết thúc sớm hợp đồng đang hoạt động");
//        }
//
//
//        // Kết thúc hợp đồng sớm
//        contract.setEndDate(currentDate);
//        contract.setStatus(WorkerContractStatus.Terminated);
//        contract.setUpdatedAt(LocalDateTime.now());
//
//        return _workerContractRepository.save(contract);
//    }

    @Override
    public Boolean CheckExistsActiveWorkerContractByWorkerId(long workerId) {
        LocalDate currentDate = LocalDate.now();
        return _workerContractRepository.findByWorkerIdAndIsDeletedFalse(workerId)
                .stream()
                .anyMatch(contract -> !contract.getStartDate().isAfter(currentDate) && contract.getEndDate().isAfter(currentDate));
        // currentDate >= startDate && currentDate < endDate
    }

    private WorkerContractStatus GetContractStatusBasedOnCurrentDate(WorkerContract contract) {
        LocalDate currentDate = LocalDate.now();
        LocalDate startDate = contract.getStartDate();
        LocalDate endDate = contract.getEndDate();
        if (currentDate.isBefore(startDate)) {
            // currentDate < startDate
            return WorkerContractStatus.Inactive;
        } else if (currentDate.isBefore(endDate)) {
            // startDate <= currentDate < endDate
            return WorkerContractStatus.Active;
        } else {
            // currentDate >= endDate
            return WorkerContractStatus.Expired;
        }
    }
}
