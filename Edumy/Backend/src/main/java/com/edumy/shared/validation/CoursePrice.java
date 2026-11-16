package com.edumy.shared.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * Course price rule BR-07: 0 (free) or at least 10 000 VND and a multiple of 1 000.
 */
@Documented
@Constraint(validatedBy = CoursePriceValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CoursePrice {

    String message() default "Giá khóa học phải bằng 0 (miễn phí) hoặc tối thiểu 10.000 VNĐ và là bội số của 1.000 VNĐ.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
