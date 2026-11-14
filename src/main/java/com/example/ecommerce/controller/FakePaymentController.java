package com.example.ecommerce.controller;

import com.example.ecommerce.payment.FakePaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fake-payment")
public class FakePaymentController {

    private final FakePaymentService fakePaymentService;

    public FakePaymentController(FakePaymentService fakePaymentService) {
        this.fakePaymentService = fakePaymentService;
    }

    @PostMapping("/{paymentId}/pay")
    public ResponseEntity<String> pay(@PathVariable String paymentId) {
        boolean success = fakePaymentService.markAsPaid(paymentId);

        if (!success) {
            return ResponseEntity.status(404).body("Payment not found");
        }

        return ResponseEntity.ok("Payment successful");
    }
}