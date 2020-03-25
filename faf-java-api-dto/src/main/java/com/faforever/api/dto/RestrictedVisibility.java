package com.faforever.api.dto;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a class or class field is only visible for users with additional permissions
 */
@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.SOURCE)
public @interface RestrictedVisibility {
  /**
   * @return an array of API permissions which are allowed to query this field
   */
  String[] value();
}
