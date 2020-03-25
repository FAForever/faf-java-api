package com.faforever.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@Type(Map.TYPE)
public class Map extends AbstractEntity {
  public static final String TYPE = "map";

  private String battleType;
  private String displayName;
  private String mapType;

  @Relationship("author")
  private Player author;

  @Relationship("statistics")
  private MapStatistics statistics;

  @Relationship("latestVersion")
  @JsonIgnore
  private MapVersion latestVersion;

  @Relationship("versions")
  @JsonIgnore
  private List<MapVersion> versions;
}
