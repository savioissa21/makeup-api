package com.hygor.makeup_api.service;

import com.hygor.makeup_api.model.Address;
import com.hygor.makeup_api.repository.AddressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AddressService extends BaseService<Address, AddressRepository> {

    public AddressService(AddressRepository repository) {
        super(repository);
    }

    @Transactional
    public Address setDefaultAddress(Long userId, Long addressId) {
        var addresses = repository.findByUserEmail(null); // No futuro usar ID do user logado
        addresses.forEach(a -> a.setDefault(false));
        
        Address defaultAddress = findActiveById(addressId);
        defaultAddress.setDefault(true);
        
        repository.saveAll(addresses);
        return repository.save(defaultAddress);
    }
}