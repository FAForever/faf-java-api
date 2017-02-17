package com.faforever.api.data.validation;

import com.faforever.api.data.domain.Clan;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class IsLeaderInClanValidator implements ConstraintValidator<IsLeaderInClan, Clan> {

  @Override
  public void initialize(IsLeaderInClan constraintAnnotation) {
    //Comment for codacy to show that this constructor is intentional empty
  }

  @Override
  public boolean isValid(Clan clan, ConstraintValidatorContext context) {
    return clan.getMemberships().stream()
        .anyMatch((membership -> clan.getLeader().getId() == membership.getPlayer().getId()));
  }
}
