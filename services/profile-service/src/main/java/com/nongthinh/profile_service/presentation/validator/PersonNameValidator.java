package com.nongthinh.profile_service.presentation.validator;

import java.util.Objects;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PersonNameValidator implements ConstraintValidator<PersonNameConstraint, String> {

    private int min;
    private int max;
    private String type;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (Objects.isNull(value))
            return true;

        value = value.trim();
        if (value.isEmpty())
            return true;

        if (value.length() < min || value.length() > max)
            return false;

        return true;
    }

    @Override
    public void initialize(PersonNameConstraint constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        this.min = constraintAnnotation.min();
        this.max = constraintAnnotation.max();
        this.type = constraintAnnotation.type();
    }

}
