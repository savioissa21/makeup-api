package com.hygor.makeup_api.service;

import com.hygor.makeup_api.dto.coupon.CouponRequest;
import com.hygor.makeup_api.dto.coupon.CouponResponse;
import com.hygor.makeup_api.model.Coupon;
import com.hygor.makeup_api.repository.CouponRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CouponService extends BaseService<Coupon, CouponRepository> {

    public CouponService(CouponRepository repository) {
        super(repository);
    }

    /**
     * Cria um novo cupão a partir de um DTO e devolve a resposta formatada.
     */
    @Transactional
    public CouponResponse createCoupon(CouponRequest request) {
        if (repository.findByCodeIgnoreCaseAndDeletedFalse(request.getCode()).isPresent()) {
            throw new RuntimeException("Já existe um cupão ativo com este código.");
        }

        Coupon coupon = Coupon.builder()
                .code(request.getCode().toUpperCase())
                .discountPercentage(request.getDiscountPercentage())
                .expirationDate(request.getExpirationDate())
                .usageLimit(request.getUsageLimit())
                .active(true)
                .build();

        Coupon saved = repository.save(coupon);
        log.info("Novo cupão criado: {} com {}% de desconto", saved.getCode(), saved.getDiscountPercentage());
        return mapToResponse(saved);
    }

    /**
     * Procura um cupão pelo código para validação na vitrine.
     */
    @Transactional(readOnly = true)
    public CouponResponse findByCode(String code) {
        Coupon coupon = repository.findByCodeIgnoreCaseAndDeletedFalse(code)
                .orElseThrow(() -> new RuntimeException("Cupão não encontrado ou inexistente."));
        return mapToResponse(coupon);
    }

    /**
     * Lista todos os cupons ativos para o painel de administração.
     */
    @Transactional(readOnly = true)
    public List<CouponResponse> getAllCoupons() {
        return repository.findAllByDeletedFalse().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Mapeador interno para seguir o padrão da boutique.
     */
    public CouponResponse mapToResponse(Coupon coupon) {
        if (coupon == null) return null;
        return CouponResponse.builder()
                .id(coupon.getId())
                .code(coupon.getCode())
                .discountPercentage(coupon.getDiscountPercentage())
                .expirationDate(coupon.getExpirationDate())
                .active(coupon.isActive())
                .usageLimit(coupon.getUsageLimit())
                .usedCount(coupon.getUsedCount())
                .valid(coupon.isValid())
                .build();
    }
}