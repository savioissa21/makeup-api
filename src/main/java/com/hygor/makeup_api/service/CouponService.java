package com.hygor.makeup_api.service;

import com.hygor.makeup_api.dto.coupon.CouponRequest;
import com.hygor.makeup_api.dto.coupon.CouponResponse;
import com.hygor.makeup_api.exception.custom.BusinessException;
import com.hygor.makeup_api.exception.custom.ResourceNotFoundException;
import com.hygor.makeup_api.mapper.CouponMapper; // Injeção
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

    private final CouponMapper couponMapper;

    public CouponService(CouponRepository repository, CouponMapper couponMapper) {
        super(repository);
        this.couponMapper = couponMapper;
    }

    @Transactional
    public CouponResponse createCoupon(CouponRequest request) {
        if (repository.findByCodeIgnoreCaseAndDeletedFalse(request.getCode()).isPresent()) {
            throw new BusinessException("Já existe um cupão ativo com o código: " + request.getCode());
        }

        Coupon coupon = Coupon.builder()
                .code(request.getCode().toUpperCase())
                .discountPercentage(request.getDiscountPercentage())
                .expirationDate(request.getExpirationDate())
                .usageLimit(request.getUsageLimit())
                .active(true)
                .usedCount(0)
                .build();

        Coupon saved = repository.save(coupon);
        log.info("Novo cupão criado: {} ({}%)", saved.getCode(), saved.getDiscountPercentage());
        
        return couponMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public CouponResponse findByCode(String code) {
        return repository.findByCodeIgnoreCaseAndDeletedFalse(code)
                .map(couponMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Cupão não encontrado: " + code));
    }

    @Transactional(readOnly = true)
    public List<CouponResponse> getAllCoupons() {
        return repository.findAllByDeletedFalse().stream()
                .map(couponMapper::toResponse)
                .collect(Collectors.toList());
    }
}