package com.hygor.makeup_api.repository;

import com.hygor.makeup_api.model.Coupon;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CouponRepository extends BaseEntityRepository<Coupon, Long> {
    Optional<Coupon> findByCodeIgnoreCaseAndDeletedFalse(String code);
}