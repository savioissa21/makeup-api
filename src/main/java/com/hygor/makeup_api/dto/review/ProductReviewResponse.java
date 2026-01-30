package com.hygor.makeup_api.dto.review;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ProductReviewResponse {
    private String customerName; // Exibimos apenas o nome, ocultando o e-mail por privacidade
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}