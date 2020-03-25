package com.faforever.api.dto;

import com.faforever.api.elide.ElideEntity;
import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@Type(CoopMission.TYPE)
public class CoopMission implements ElideEntity {
  public static final String TYPE = "coopMission";

  @Id
  @EqualsAndHashCode.Include
  private String id;
  private String name;
  private int version;
  private String category;
  private String thumbnailUrlSmall;
  private String thumbnailUrlLarge;
  private String description;
  private String downloadUrl;
  private String folderName;
}
