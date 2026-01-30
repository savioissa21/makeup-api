package com.hygor.makeup_api.service;

import com.hygor.makeup_api.dto.address.AddressRequest;
import com.hygor.makeup_api.dto.address.AddressResponse;
import com.hygor.makeup_api.model.Address;
import com.hygor.makeup_api.model.User;
import com.hygor.makeup_api.repository.AddressRepository;
import com.hygor.makeup_api.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects; // Importante para comparação segura

@Service
public class AddressService extends BaseService<Address, AddressRepository> {

    private final UserRepository userRepository;

    public AddressService(AddressRepository repository, UserRepository userRepository) {
        super(repository);
        this.userRepository = userRepository;
    }

    @Transactional
    public AddressResponse createAddress(AddressRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

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

        if (request.isDefault()) {
            handleDefaultAddress(currentUser.getEmail(), null);
        }

        return toResponse(repository.save(address));
    }

    @Transactional
    public AddressResponse setDefaultAddress(Long addressId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        
        Address address = repository.findById(addressId)
                .filter(a -> a.getUser().getEmail().equals(email))
                .orElseThrow(() -> new RuntimeException("Endereço não encontrado ou acesso negado"));

        handleDefaultAddress(email, addressId);
        
        address.setDefault(true);
        return toResponse(repository.save(address));
    }

    private void handleDefaultAddress(String email, Long currentAddressId) {
        List<Address> addresses = repository.findByUserEmail(email);
        addresses.forEach(a -> {
            // CORREÇÃO: Comparação segura de IDs para evitar NullPointerException
            if (!Objects.equals(a.getId(), currentAddressId)) {
                a.setDefault(false);
            }
        });
        repository.saveAll(addresses);
    }

    // RENOMEADO: toResponse para seguir o padrão da boutique
    public AddressResponse toResponse(Address address) {
        if (address == null) return null;
        return AddressResponse.builder()
                .id(address.getId())
                .street(address.getStreet())
                .number(address.getNumber())
                .zipCode(address.getZipCode())
                .city(address.getCity())
                .state(address.getState())
                .complement(address.getComplement())
                .isDefault(address.isDefault())
                .build();
    }
    @Transactional(readOnly = true)
public List<AddressResponse> getMyAddresses() {
    String email = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
    return repository.findByUserEmail(email).stream()
            .map(this::toResponse)
            .collect(java.util.stream.Collectors.toList());
}
}