package com.faforever.api.data.validation;


import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({TYPE, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = VotingSubjectRevealWinnerValidator.class)
@Documented
public @interface VotingSubjectRevealWinnerCheck {

  String message() default "Cannot reveal voting result before end of voting period";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
