package com.faforever.api.data.domain;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class LicenseTest {

  @ParameterizedTest
  @CsvSource({
    "true,true",
    "false,false",
    "true, false",
    "false, true"
  })
  void unchangedLicenseIsNeverLessPermissive(boolean modifiable, boolean redistributable) {
    License currentState = new License();
    currentState.setModifiable(modifiable);
    currentState.setRedistributable(redistributable);

    License newState = new License();
    newState.setModifiable(modifiable);
    newState.setRedistributable(redistributable);

    assertFalse(currentState.isLessPermissiveThan(newState) );
  }

  @ParameterizedTest
  @CsvSource({
    // we intentionally miss true/true because it would be equal
    "false,false",
    "true, false",
    "false, true"
  })
  void maximumPermissiveLicenseIsNeverLessPermissiveAndReflexiveInverse(boolean modifiable, boolean redistributable) {
    License currentState = new License();
    currentState.setModifiable(true);
    currentState.setRedistributable(true);

    License newState = new License();
    newState.setModifiable(modifiable);
    newState.setRedistributable(redistributable);

    assertFalse(currentState.isLessPermissiveThan(newState) );
    // test the inverse reflexive
    assertTrue(newState.isLessPermissiveThan(currentState) );
  }

  @ParameterizedTest
  @CsvSource({
    "true,true",
    "true, false",
    "false, true"
    // we intentionally miss false/false because it would be equal
  })
  void nonPermissiveLicenseIsLessPermissiveAndReflexiveInverse(boolean modifiable, boolean redistributable) {
    License currentState = new License();
    currentState.setModifiable(false);
    currentState.setRedistributable(false);

    License newState = new License();
    newState.setModifiable(modifiable);
    newState.setRedistributable(redistributable);

    assertTrue(currentState.isLessPermissiveThan(newState));
    // test the inverse reflexive
    assertFalse(newState.isLessPermissiveThan(currentState) );
  }
}
