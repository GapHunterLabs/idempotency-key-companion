package com.acmecorp.orders;

public class OrderController {

    // Missing idempotency key -- a retried request creates a duplicate order.
    @PostMapping("/orders")
    public Order createOrder(@RequestBody CreateOrderRequest request) {
        return orderService.create(request);
    }

    // Correct: has an Idempotency-Key header parameter.
    @PostMapping("/payments")
    public Payment createPayment(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody CreatePaymentRequest request) {
        return paymentService.charge(idempotencyKey, request);
    }
}
