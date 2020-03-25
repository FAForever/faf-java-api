package com.faforever.api.dto;

import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@Type(MapVersionReview.TYPE)
public class MapVersionReview extends Review {
  public static final String TYPE = "mapVersionReview";

  @Relationship("mapVersion")
  private MapVersion mapVersion;
}
