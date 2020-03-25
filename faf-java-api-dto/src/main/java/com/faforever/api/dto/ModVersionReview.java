package com.faforever.api.dto;

import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@Type(ModVersionReview.TYPE)
public class ModVersionReview extends Review {
  public static final String TYPE = "modVersionReview";

  @Relationship("modVersion")
  private ModVersion modVersion;
}
