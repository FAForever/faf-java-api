package com.faforever.api.dto;

import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.util.List;


@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@Type(MapVersion.TYPE)
public class MapVersion extends AbstractEntity {
  public static final String TYPE = "mapVersion";

  private String description;
  private Integer maxPlayers;
  private Integer width;
  private Integer height;
  private Integer version;
  private String folderName;
  // TODO name consistently with folderName
  private String filename;
  private boolean ranked;
  private boolean hidden;
  private URL thumbnailUrlSmall;
  private URL thumbnailUrlLarge;
  private URL downloadUrl;

  @Relationship("map")
  private Map map;

  @Relationship("statistics")
  private MapVersionStatistics statistics;

  @Nullable
  @Relationship("ladder1v1Map")
  private Ladder1v1Map ladder1v1Map;

  @Relationship("reviews")
  private List<MapVersionReview> reviews;
}
