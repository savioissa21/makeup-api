package com.hygor.makeup_api.service;

import com.hygor.makeup_api.model.Payment;
import com.hygor.makeup_api.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService extends BaseService<Payment, PaymentRepository> {

    public PaymentService(PaymentRepository repository) {
        super(repository);
    }

    @Transactional
    public Payment processPayment(Payment payment) {
        // Aqui entraria a integração com Mercado Pago ou Stripe
        return repository.save(payment);
    }
}