package com.aad.microservice.worker_service.service;


import com.aad.microservice.worker_service.exception.AppException;
import com.aad.microservice.worker_service.exception.ErrorCode;
import com.aad.microservice.worker_service.model.Worker;
import com.aad.microservice.worker_service.repository.WorkerRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class WorkerServiceImpl implements IWorkerService {
    private final WorkerRepository _workerRepository;

    public WorkerServiceImpl(WorkerRepository workerRepository){
        _workerRepository = workerRepository;
    }


    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return Pattern.matches(emailRegex, email);
    }

    private boolean isValidIdentityCardNo(String identityCardNo) {
        // Căn cước công dân có 12 số
        return identityCardNo != null && identityCardNo.matches("\\d{12}");
    }

    @Override
    public Worker createWorker(Worker worker) {
        worker.setCreatedAt(LocalDateTime.now());
        worker.setIsDeleted(false);
        boolean existedEmail = _workerRepository.existsByEmailAndIsDeletedFalse(worker.getEmail());
        if(existedEmail){
            throw new AppException(ErrorCode.Duplicated_Exception, "Email đã tồn tại");
        }

        boolean existedIdentityCardNo = _workerRepository.existsByIdentityCardNoAndIsDeletedFalse(worker.getIdentityCardNo());
        if(existedIdentityCardNo){
            throw new AppException(ErrorCode.Duplicated_Exception, "Số CCCD đã tồn tại");
        }

        if(!isValidEmail(worker.getEmail())) {
            throw new AppException(ErrorCode.NotAllowCreate_Exception, "Email không hợp lệ");
        }

        if(!isValidIdentityCardNo(worker.getIdentityCardNo())){
            throw new AppException(ErrorCode.NotAllowCreate_Exception, "Số CCCD không hợp lệ");
        }

        return _workerRepository.save(worker);
    }

    @Override
    public Worker updateWorker(Worker worker) {
        Optional<Worker> existedWorker = _workerRepository.findById(worker.getId());

        if(existedWorker.isEmpty() || existedWorker.get().getIsDeleted()) {
            throw new AppException(ErrorCode.NotFound_Exception, "Không tìm thấy thông tin nhân công");
        }

        boolean existedEmail = _workerRepository.existsByEmailAndIsDeletedFalseAndIdNot(worker.getEmail(), worker.getId());
        if(existedEmail){
            throw new AppException(ErrorCode.Duplicated_Exception, "Email đã tồn tại");
        }

        boolean existedIdentityCardNo = _workerRepository.existsByIdentityCardNoAndIsDeletedFalseAndIdNot(worker.getIdentityCardNo(), worker.getId());
        if(existedIdentityCardNo){
            throw new AppException(ErrorCode.Duplicated_Exception, "Số CCCD đã tồn tại");
        }

        if(!isValidEmail(worker.getEmail())) {
            throw new AppException(ErrorCode.NotAllowCreate_Exception, "Email không hợp lệ");
        }

        if(!isValidIdentityCardNo(worker.getIdentityCardNo())){
            throw new AppException(ErrorCode.NotAllowCreate_Exception, "Số CCCD không hợp lệ");
        }

        existedWorker.get().setFullName(worker.getFullName());
        existedWorker.get().setPhoneNumber(worker.getPhoneNumber());
        existedWorker.get().setEmail(worker.getEmail());
        existedWorker.get().setAddress(worker.getAddress());
        existedWorker.get().setGender(worker.getGender());
        existedWorker.get().setDob(worker.getDob());
        existedWorker.get().setUpdatedAt(LocalDateTime.now());
        return _workerRepository.save(existedWorker.get());
    }

    @Override
    public void deleteWorker(Long id) {
        Optional<Worker> worker = _workerRepository.findById(id);
        if(worker.isEmpty() || worker.get().getIsDeleted()) {
            throw new AppException(ErrorCode.NotFound_Exception, "Không tìm thấy thông tin nhân công");
        }

        worker.get().setIsDeleted(true);
        _workerRepository.save(worker.get());
    }

    @Override
    public Worker getWorkerById(Long id){
        Optional<Worker> worker = _workerRepository.findById(id);
        if(worker.isEmpty() || worker.get().getIsDeleted()) {
            throw new AppException(ErrorCode.NotFound_Exception, "Không tìm thấy thông tin nhân công");
        }
        return worker.get();
    }

    @Override
    public List<Worker> getAllWorkers() {
        return _workerRepository.findByIsDeletedFalse();
    }

    @Override
    public boolean checkWorkerExists(Long id) {
        Optional<Worker> worker = _workerRepository.findById(id);
        return worker.isPresent() && !worker.get().getIsDeleted();
    }
}
