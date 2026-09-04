package com.skillnet.serializedcoupon.domain;

import java.util.regex.Pattern;

public final class CouponCodes {

    public static final String PREFIX = "FF";
    public static final int PROGRAM_CODE_LENGTH = 4;
    public static final int SUFFIX_LENGTH = 8;
    public static final int TOTAL_LENGTH = 14;
    public static final String PROGRAM_CODE_REGEX = "^[0-9]{4}$";
    public static final String COUPON_CODE_REGEX = "^FF[0-9]{4}[A-Z0-9]{8}$";
    public static final String SUFFIX_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    public static final Pattern PROGRAM_CODE_PATTERN = Pattern.compile(PROGRAM_CODE_REGEX);
    public static final Pattern COUPON_CODE_PATTERN = Pattern.compile(COUPON_CODE_REGEX);

    public static final String AMBIGUOUS_EXCLUDED = "I, O, 0, 1";

    private CouponCodes() {
    }

    public static boolean isValidProgramCode(String couponProgramCode) {
        return couponProgramCode != null && PROGRAM_CODE_PATTERN.matcher(couponProgramCode).matches();
    }

    public static boolean isValidCouponCode(String couponCode) {
        return couponCode != null && COUPON_CODE_PATTERN.matcher(couponCode).matches();
    }
}
