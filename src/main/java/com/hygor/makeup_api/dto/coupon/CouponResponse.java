package com.hygor.makeup_api.dto.coupon;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class CouponResponse {
    private Long id;
    private String code;
    private Double discountPercentage;
    private LocalDateTime expirationDate;
    private boolean active;
    private Integer usageLimit;
    private Integer usedCount;
    private boolean valid;
}