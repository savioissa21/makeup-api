package com.hygor.makeup_api.repository;

import com.hygor.makeup_api.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Interface de acesso a dados para transacções financeiras.
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Procura um pagamento pelo ID externo (ex: do processador de pagamentos).
     */
    Optional<Payment> findByExternalId(String externalId);
}