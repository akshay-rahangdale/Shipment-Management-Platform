package com.shipment.shipmentservice.service;

import com.shipment.shipmentservice.dto.response.CustomerResponse;
import com.shipment.shipmentservice.mapper.ShipmentMapper;
import com.shipment.shipmentservice.model.Customer;
import com.shipment.shipmentservice.repository.CustomerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
 
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CustomerService {
    

    private final CustomerRepository customerRepository;
    private final ShipmentMapper mapper;

    public Page<CustomerResponse> getCustomers(Pageable pageable) {
        return customerRepository.findAll(pageable).map(mapper::toCustomerResponse);
    }
 
    public CustomerResponse getById(UUID id) {
        Customer customer = customerRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Customer not found: " + id));

            return mapper.toCustomerResponse(customer);
    }
}
