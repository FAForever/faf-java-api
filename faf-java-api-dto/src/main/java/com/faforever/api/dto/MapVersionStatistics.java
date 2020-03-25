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
@Type(MapVersionStatistics.TYPE)
public class MapVersionStatistics implements ElideEntity {
  public static final String TYPE = "mapVersionStatistics";

  @Id
  @EqualsAndHashCode.Include
  private String id;
  private int downloads;
  private int draws;
  private int plays;

  @Relationship("mapVersion")
  private MapVersion mapVersion;
}
