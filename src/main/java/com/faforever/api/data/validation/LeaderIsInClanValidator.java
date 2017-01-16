package com.faforever.api.data.validation;

import com.faforever.api.data.domain.Clan;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class LeaderIsInClanValidator implements ConstraintValidator<LeaderIsInClan, Clan> {

  @Override
  public void initialize(LeaderIsInClan constraintAnnotation) {

  }

  @Override
  public boolean isValid(Clan clan, ConstraintValidatorContext context) {
    return clan.getMemberships().stream()
        .anyMatch((membership -> clan.getLeader().getId() == membership.getPlayer().getId()));
  }
}
