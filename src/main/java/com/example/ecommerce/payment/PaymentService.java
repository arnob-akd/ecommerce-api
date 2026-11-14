package com.example.ecommerce.payment;

public interface PaymentService
{

    String createPayment(double amount);

    boolean isPaid(String paymentId);
}