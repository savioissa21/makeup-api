package com.hygor.makeup_api.dto.coupon;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CouponRequest {

    @NotBlank(message = "O código do cupão é obrigatório (ex: NATAL2024)")
    @Size(min = 3, max = 20, message = "O código deve ter entre 3 e 20 caracteres")
    private String code;

    @NotNull(message = "A percentagem de desconto é obrigatória")
    @DecimalMin(value = "0.01", message = "O desconto deve ser no mínimo 0.01%")
    @DecimalMax(value = "100.00", message = "O desconto não pode exceder 100%")
    private Double discountPercentage;

    @NotNull(message = "A data de expiração é obrigatória")
    @Future(message = "A data de expiração deve ser no futuro")
    private LocalDateTime expirationDate;

    @Positive(message = "O limite de uso deve ser um número positivo")
    private Integer usageLimit;
}