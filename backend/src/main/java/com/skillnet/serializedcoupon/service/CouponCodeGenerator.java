package com.skillnet.serializedcoupon.service;

import com.skillnet.serializedcoupon.domain.CouponCodes;
import com.skillnet.serializedcoupon.exception.BusinessValidationException;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class CouponCodeGenerator {

    private final SecureRandom secureRandom = new SecureRandom();

    public String generate(String couponProgramCode) {
        if (!CouponCodes.isValidProgramCode(couponProgramCode)) {
            throw new BusinessValidationException("couponProgramCode must be exactly 4 digits");
        }
        String suffix = randomSuffix();
        String code = CouponCodes.PREFIX + couponProgramCode + suffix;
        if (!CouponCodes.isValidCouponCode(code)) {
            throw new IllegalStateException("Generated coupon code failed format validation");
        }
        return code;
    }

    private String randomSuffix() {
        String alphabet = CouponCodes.SUFFIX_ALPHABET;
        StringBuilder suffix = new StringBuilder(CouponCodes.SUFFIX_LENGTH);
        for (int i = 0; i < CouponCodes.SUFFIX_LENGTH; i++) {
            int index = secureRandom.nextInt(alphabet.length());
            suffix.append(alphabet.charAt(index));
        }
        return suffix.toString();
    }
}
