package com.hygor.makeup_api.dto.address;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddressResponse {
    private Long id;
    private Long userId;
    private String street;
    private String number;
    private String zipCode;
    private String city;
    private String state;
    private String complement;
    private boolean isDefault;
}