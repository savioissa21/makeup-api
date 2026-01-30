package com.hygor.makeup_api.dto.review;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ProductReviewResponse {
    private Long id;            // ADICIONADO: O Java precisa disto para o Builder funcionar
    private Long productId;     // ADICIONADO: Para sabermos de qual produto é a review
    private String customerName; 
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}