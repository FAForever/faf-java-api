package com.faforever.api.data.validation;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({TYPE, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = IsLeaderInClanValidator.class)
@Documented
public @interface IsLeaderInClan {

  String message() default "Clan Leader is not Clan Member";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
