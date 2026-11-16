package com.edumy.shared.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.math.BigDecimal;

/**
 * Validates course price rule (BR-07): 0 (free) or at least 10,000 VND and a multiple of 1,000 VND.
 * Supports both Long and BigDecimal.
 */
public class CoursePriceValidator implements ConstraintValidator<CoursePrice, Object> {

    static final long MIN_PAID_PRICE = 10_000L;
    static final long PRICE_STEP = 1_000L;

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        long priceLong;
        if (value instanceof BigDecimal bd) {
            priceLong = bd.longValue();
        } else if (value instanceof Number num) {
            priceLong = num.longValue();
        } else {
            return false;
        }

        return priceLong == 0 || (priceLong >= MIN_PAID_PRICE && priceLong % PRICE_STEP == 0);
    }
}
