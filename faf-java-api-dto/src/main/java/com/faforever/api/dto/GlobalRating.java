package com.faforever.api.dto;

import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@Type(GlobalRating.TYPE)
@SuperBuilder
public class GlobalRating extends Rating {
  public static final String TYPE = "globalRating";
}
