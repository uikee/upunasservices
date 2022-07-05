package com.upuna.customer.service;

import com.upuna.clients.fraud.FraudCheckResponse;
import com.upuna.clients.fraud.FraudClient;
import com.upuna.clients.notification.NotificationClient;
import com.upuna.clients.notification.NotificationRequest;
import com.upuna.customer.CustomerRegistrationRequest;
import com.upuna.customer.model.Customer;
import com.upuna.customer.repository.CustomerRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@AllArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final FraudClient fraudClient;
    private final NotificationClient notificationClient;

    public void registerCustomer(CustomerRegistrationRequest customerRegistrationRequest) {
        Customer customer = Customer.builder()
                .firstName(customerRegistrationRequest.firstName())
                .lastName(customerRegistrationRequest.lastName())
                .email(customerRegistrationRequest.email())
                .build();

        customerRepository.saveAndFlush(customer);

        FraudCheckResponse fraudCheckResponse = fraudClient
                .isFraudster(customer.getId());

        if(fraudCheckResponse.isFraudster())
            throw new IllegalStateException("fraudster");

        notificationClient.sendNotification(
                new NotificationRequest(
                        customer.getId(),
                        customer.getEmail(),
                        String.format("Hi %s, welcome to UpunaServices...",
                                customer.getFirstName())
                )
        );
    }
}
