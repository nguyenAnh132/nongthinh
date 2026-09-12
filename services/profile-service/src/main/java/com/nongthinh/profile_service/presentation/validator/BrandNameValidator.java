package com.nongthinh.profile_service.presentation.validator;

import java.util.Objects;
import java.util.regex.Pattern;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class BrandNameValidator implements ConstraintValidator<BrandNameConstraint, String> {

    private static final Pattern FORMAT = Pattern.compile("^[\\p{L}\\p{N}][\\p{L}\\p{N}\\s&.,\\-'\"()]*$");

    private int min;
    private int max;

    @Override
    public void initialize(BrandNameConstraint constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        this.min = constraintAnnotation.min();
        this.max = constraintAnnotation.max();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (Objects.isNull(value)) {
            return true;
        }

        value = value.trim();
        if (value.isEmpty()) {
            return true;
        }

        if (value.length() < min || value.length() > max) {
            return false;
        }

        if (!FORMAT.matcher(value).matches()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("BRAND_NAME_FORMAT_INVALID")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
