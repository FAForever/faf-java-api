package com.faforever.api.data.validation;

import com.faforever.api.data.domain.VotingSubject;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.OffsetDateTime;

/**
 * Avoids that a VotingSubject's result is revealed before the vote on the subject ended
 */
public class VotingSubjectRevealWinnerValidator implements ConstraintValidator<VotingSubjectRevealWinnerCheck, VotingSubject> {

  @Override
  public void initialize(VotingSubjectRevealWinnerCheck constraintAnnotation) {
    // Comment for codacy to show that this constructor is intentional empty
  }

  @Override
  public boolean isValid(VotingSubject votingSubject, ConstraintValidatorContext context) {
    return votingSubject.getRevealWinner() != Boolean.TRUE || votingSubject.getEndOfVoteTime().isBefore(OffsetDateTime.now());
  }
}
