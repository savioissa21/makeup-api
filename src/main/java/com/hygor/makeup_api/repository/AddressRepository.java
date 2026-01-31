package com.hygor.makeup_api.repository;

import com.hygor.makeup_api.model.Address;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

/**
 * Interface de acesso a dados para endereços de envio e faturação.
 */
@Repository
public interface AddressRepository extends BaseEntityRepository<Address, Long> {

    /**
     * Lista todos os endereços guardados de um utilizador específico.
     */
    List<Address> findByUserEmail(String email);

    /**
     * Procura o endereço marcado como predefinido para um utilizador.
     */
    Optional<Address> findByUserIdAndIsDefaultTrue(Long userId);

    /**
     * Conta quantos endereços o utilizador tem (Útil para validar limites).
     * O Spring Data implementa isto automaticamente com base no nome.
     */
    long countByUserEmail(String email);
}