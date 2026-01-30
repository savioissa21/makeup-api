package com.hygor.makeup_api.dto.address;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AddressRequest {
    @NotBlank(message = "A rua é obrigatória")
    private String street;

    @NotBlank(message = "O número é obrigatório")
    private String number;

    @NotBlank(message = "O código postal é obrigatório")
    @Pattern(regexp = "\\d{5}-\\d{3}", message = "Formato de CEP inválido (ex: 12345-678)")
    private String zipCode;

    @NotBlank(message = "A cidade é obrigatória")
    private String city;

    @NotBlank(message = "O estado é obrigatório")
    private String state;

    private String complement;

    private boolean isDefault;
}