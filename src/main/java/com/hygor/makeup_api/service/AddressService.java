package com.hygor.makeup_api.service;

import com.hygor.makeup_api.dto.address.AddressRequest;
import com.hygor.makeup_api.dto.address.AddressResponse;
import com.hygor.makeup_api.exception.custom.BusinessException;
import com.hygor.makeup_api.exception.custom.ResourceNotFoundException;
import com.hygor.makeup_api.mapper.AddressMapper; // Injeção do Mapper
import com.hygor.makeup_api.model.Address;
import com.hygor.makeup_api.model.User;
import com.hygor.makeup_api.repository.AddressRepository;
import com.hygor.makeup_api.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AddressService extends BaseService<Address, AddressRepository> {

    private final UserRepository userRepository;
    private final AddressMapper addressMapper; // Mapper injetado

    public AddressService(AddressRepository repository, 
                          UserRepository userRepository, 
                          AddressMapper addressMapper) {
        super(repository);
        this.userRepository = userRepository;
        this.addressMapper = addressMapper;
    }

    @Transactional
    public AddressResponse createAddress(AddressRequest request) {
        String email = getCurrentUserEmail();
        
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + email));

        // Validação de Regra de Negócio (Exemplo: Limite de endereços)
        if (repository.countByUserEmail(email) >= 10) {
            throw new BusinessException("Limite de endereços atingido. Remova um antigo para adicionar novo.");
        }

        Address address = Address.builder()
                .street(request.getStreet())
                .number(request.getNumber())
                .zipCode(request.getZipCode())
                .city(request.getCity())
                .state(request.getState())
                .complement(request.getComplement())
                .isDefault(request.isDefault())
                .user(currentUser)
                .build();

        // Se for o primeiro endereço, força como padrão
        if (repository.countByUserEmail(email) == 0) {
            address.setDefault(true);
        } else if (request.isDefault()) {
            handleDefaultAddress(email, null);
        }

        return addressMapper.toResponse(repository.save(address));
    }

    @Transactional
    public AddressResponse setDefaultAddress(Long addressId) {
        String email = getCurrentUserEmail();

        Address address = repository.findById(addressId)
                .filter(a -> a.getUser().getEmail().equals(email)) // Segurança: Garante que o endereço é do user
                .orElseThrow(() -> new ResourceNotFoundException("Endereço não encontrado ou não pertence a este usuário."));

        if (address.isDefault()) {
            return addressMapper.toResponse(address); // Já é padrão, não faz nada
        }

        handleDefaultAddress(email, addressId);
        
        address.setDefault(true);
        return addressMapper.toResponse(repository.save(address));
    }

    @Transactional(readOnly = true)
    public List<AddressResponse> getMyAddresses() {
        String email = getCurrentUserEmail();
        return repository.findByUserEmail(email).stream()
                .map(addressMapper::toResponse) // Conversão limpa via Method Reference
                .collect(Collectors.toList());
    }

    /**
     * Garante que apenas um endereço seja o padrão.
     * Remove o flag 'default' de todos os outros.
     */
    private void handleDefaultAddress(String email, Long currentAddressId) {
        List<Address> addresses = repository.findByUserEmail(email);
        addresses.forEach(a -> {
            if (!Objects.equals(a.getId(), currentAddressId) && a.isDefault()) {
                a.setDefault(false);
            }
        });
        repository.saveAll(addresses);
    }
    
    // Método auxiliar para pegar o email do contexto de segurança
    private String getCurrentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}