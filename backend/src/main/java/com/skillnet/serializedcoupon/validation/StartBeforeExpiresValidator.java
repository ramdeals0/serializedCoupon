package com.skillnet.serializedcoupon.validation;

import com.skillnet.serializedcoupon.dto.HasValidityWindow;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class StartBeforeExpiresValidator implements ConstraintValidator<StartBeforeExpires, HasValidityWindow> {

    @Override
    public boolean isValid(HasValidityWindow value, ConstraintValidatorContext context) {
        if (value == null || value.startAt() == null || value.expiresAt() == null) {
            return true;
        }
        return value.startAt().isBefore(value.expiresAt());
    }
}
