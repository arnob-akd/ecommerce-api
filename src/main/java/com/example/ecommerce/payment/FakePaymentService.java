package com.example.ecommerce.payment;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FakePaymentService implements PaymentService {

    private final Map<String, Boolean> payments = new ConcurrentHashMap<>();

    @Override
    public String createPayment(double amount) {
        String paymentId = UUID.randomUUID().toString();
        payments.put(paymentId, false);
        return paymentId;
    }

    @Override
    public boolean isPaid(String paymentId) {
        return payments.getOrDefault(paymentId, false);
    }

    public boolean markAsPaid(String paymentId) {
        if (!payments.containsKey(paymentId)) {
            return false;
        }
        payments.put(paymentId, true);
        return true;
    }
}