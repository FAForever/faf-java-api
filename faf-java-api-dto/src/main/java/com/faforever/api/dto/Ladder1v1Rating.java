package com.faforever.api.dto;

import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@Type(Ladder1v1Rating.TYPE)
public class Ladder1v1Rating extends Rating {
  public static final String TYPE = "ladder1v1Rating";
}
