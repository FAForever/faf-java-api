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
@Type(MapStatistics.TYPE)
public class MapStatistics implements ElideEntity {
  public static final String TYPE = "mapStatistics";

  @Id
  @EqualsAndHashCode.Include
  private String id;
  private int downloads;
  private int draws;
  private int plays;

  @Relationship("map")
  private Map map;
}
