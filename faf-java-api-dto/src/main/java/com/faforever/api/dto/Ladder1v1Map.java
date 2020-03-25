package com.faforever.api.dto;

import com.faforever.api.elide.ElideEntity;
import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@Type(Ladder1v1Map.TYPE)
public class Ladder1v1Map implements ElideEntity {
  public static final String TYPE = "ladder1v1Map";

  @Id
  @EqualsAndHashCode.Include
  private String id;
  @Relationship("mapVersion")
  private MapVersion mapVersion;
}
