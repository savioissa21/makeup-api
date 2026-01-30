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

@Service
public class AddressService extends BaseService<Address, AddressRepository> {

    private final UserRepository userRepository;

    public AddressService(AddressRepository repository, UserRepository userRepository) {
        super(repository);
        this.userRepository = userRepository;
    }

    @Transactional
    public AddressResponse createAddress(AddressRequest request) {
        // Busca o usuário logado pelo e-mail (do JWT) para garantir segurança
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // Converte DTO para Entidade
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

        // Se for o primeiro endereço ou marcado como padrão, ajusta os outros
        if (request.isDefault()) {
            handleDefaultAddress(currentUser.getEmail(), null);
        }

        Address saved = repository.save(address);
        return mapToResponse(saved);
    }

    @Transactional
    public AddressResponse setDefaultAddress(Long addressId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        
        // Garante que o endereço pertence ao usuário logado (Segurança Máxima)
        Address address = repository.findById(addressId)
                .filter(a -> a.getUser().getEmail().equals(email))
                .orElseThrow(() -> new RuntimeException("Endereço não encontrado ou acesso negado"));

        handleDefaultAddress(email, addressId);
        
        address.setDefault(true);
        return mapToResponse(repository.save(address));
    }

    private void handleDefaultAddress(String email, Long currentAddressId) {
        List<Address> addresses = repository.findByUserEmail(email);
        addresses.forEach(a -> {
            if (!a.getId().equals(currentAddressId)) {
                a.setDefault(false);
            }
        });
        repository.saveAll(addresses);
    }

    // Método de mapeamento (Poderia ser um MapStruct no futuro)
    private AddressResponse mapToResponse(Address address) {
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
}