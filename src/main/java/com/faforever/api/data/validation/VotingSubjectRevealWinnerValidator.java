package com.faforever.api.data.validation;

import com.faforever.api.data.domain.VotingSubject;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.OffsetDateTime;

public class VotingSubjectRevealWinnerValidator implements ConstraintValidator<VotingSubjectRevealWinnerCheck, VotingSubject> {

  @Override
  public void initialize(VotingSubjectRevealWinnerCheck constraintAnnotation) {
    // Comment for codacy to show that this constructor is intentional empty
  }

  @Override
  public boolean isValid(VotingSubject votingSubject, ConstraintValidatorContext context) {
    return votingSubject.getRevealWinner() && votingSubject.getEndOfVoteTime().isBefore(OffsetDateTime.now());
  }
}
