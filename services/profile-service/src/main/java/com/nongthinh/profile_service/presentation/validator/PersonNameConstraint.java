package com.nongthinh.profile_service.presentation.validator;

import java.lang.annotation.Target;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import static java.lang.annotation.ElementType.FIELD;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({FIELD})
@Retention(RUNTIME)
@Constraint(validatedBy = {PersonNameValidator.class})
public @interface PersonNameConstraint {

    String message() default "Invalid person name";

    int min() default 1;
    int max() default 200;
    String type();

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
