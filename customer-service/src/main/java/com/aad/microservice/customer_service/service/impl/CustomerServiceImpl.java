package com.aad.microservice.customer_service.service.impl;

import com.aad.microservice.customer_service.exception.AppException;
import com.aad.microservice.customer_service.exception.ErrorCode;
import com.aad.microservice.customer_service.model.Customer;
import com.aad.microservice.customer_service.repository.CustomerRepository;
import com.aad.microservice.customer_service.service.CustomerService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository customerRepository;

    public CustomerServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return Pattern.matches(emailRegex, email);
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        // Số điện thoại có 10 số
        return phoneNumber != null && phoneNumber.matches("\\d{10}");
    }

    @Override
    public Customer createCustomer(Customer customer) {
        customer.setCreatedAt(LocalDateTime.now());
        customer.setUpdatedAt(LocalDateTime.now());
        customer.setIsDeleted(false);

        boolean existedEmail = customerRepository.existsByEmailAndIsDeletedFalse(customer.getEmail());
        if (existedEmail) {
            throw new AppException(ErrorCode.Duplicated_Exception, "Email đã tồn tại");
        }

        boolean existedPhoneNumber = customerRepository.existsByPhoneNumberAndIsDeletedFalse(customer.getPhoneNumber());
        if (existedPhoneNumber) {
            throw new AppException(ErrorCode.Duplicated_Exception, "Số điện thoại đã tồn tại");
        }

        if (!isValidEmail(customer.getEmail())) {
            throw new AppException(ErrorCode.NotAllowCreate_Exception, "Email không hợp lệ");
        }

        if (!isValidPhoneNumber(customer.getPhoneNumber())) {
            throw new AppException(ErrorCode.NotAllowCreate_Exception, "Số điện thoại không hợp lệ");
        }

        return customerRepository.save(customer);
    }

    @Override
    public Customer updateCustomer(Customer customer) {
        Optional<Customer> existedCustomer = customerRepository.findById(customer.getId());

        if (existedCustomer.isEmpty() || existedCustomer.get().getIsDeleted()) {
            throw new AppException(ErrorCode.NotFound_Exception, "Không tìm thấy thông tin khách hàng");
        }

        boolean existedEmail = customerRepository.existsByEmailAndIsDeletedFalseAndIdNot(customer.getEmail(), customer.getId());
        if (existedEmail) {
            throw new AppException(ErrorCode.Duplicated_Exception, "Email đã tồn tại");
        }

        boolean existedPhoneNumber = customerRepository.existsByPhoneNumberAndIsDeletedFalseAndIdNot(customer.getPhoneNumber(), customer.getId());
        if (existedPhoneNumber) {
            throw new AppException(ErrorCode.Duplicated_Exception, "Số điện thoại đã tồn tại");
        }

        if (!isValidEmail(customer.getEmail())) {
            throw new AppException(ErrorCode.NotAllowUpdate_Exception, "Email không hợp lệ");
        }

        if (!isValidPhoneNumber(customer.getPhoneNumber())) {
            throw new AppException(ErrorCode.NotAllowUpdate_Exception, "Số điện thoại không hợp lệ");
        }

        Customer existingCustomer = existedCustomer.get();
        existingCustomer.setFullname(customer.getFullname());
        existingCustomer.setCompanyName(customer.getCompanyName());
        existingCustomer.setPhoneNumber(customer.getPhoneNumber());
        existingCustomer.setEmail(customer.getEmail());
        existingCustomer.setAddress(customer.getAddress());
        existingCustomer.setUpdatedAt(LocalDateTime.now());

        return customerRepository.save(existingCustomer);
    }

    @Override
    public void deleteCustomer(Long id) {
        Optional<Customer> customer = customerRepository.findById(id);
        if (customer.isEmpty() || customer.get().getIsDeleted()) {
            throw new AppException(ErrorCode.NotFound_Exception, "Không tìm thấy thông tin khách hàng");
        }

        customer.get().setIsDeleted(true);
        customer.get().setUpdatedAt(LocalDateTime.now());
        customerRepository.save(customer.get());
    }

    @Override
    public Customer getCustomerById(Long id) {
        Optional<Customer> customer = customerRepository.findByIdAndIsDeletedFalse(id);
        if (customer.isEmpty()) {
            throw new AppException(ErrorCode.NotFound_Exception, "Không tìm thấy thông tin khách hàng");
        }
        return customer.get();
    }

    @Override
    public List<Customer> getAllCustomers() {
        return customerRepository.findByIsDeletedFalse();
    }

    @Override
    public boolean checkCustomerExists(Long id) {
        return customerRepository.findByIdAndIsDeletedFalse(id).isPresent();
    }

    @Override
    public List<Customer> searchCustomers(String fullname, String phoneNumber) {
        // Nếu cả hai tham số đều có giá trị
        if (fullname != null && !fullname.isEmpty() && phoneNumber != null && !phoneNumber.isEmpty()) {
            return customerRepository.findByFullnameContainingIgnoreCaseAndPhoneNumberContainingAndIsDeletedFalse(fullname, phoneNumber);
        }

        // Nếu chỉ có fullname
        if (fullname != null && !fullname.isEmpty()) {
            return customerRepository.findByFullnameContainingIgnoreCaseAndIsDeletedFalse(fullname);
        }

        // Nếu chỉ có phoneNumber
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            return customerRepository.findByPhoneNumberContainingAndIsDeletedFalse(phoneNumber);
        }

        // Nếu không có tham số nào, trả về tất cả khách hàng
        return getAllCustomers();
    }
}
