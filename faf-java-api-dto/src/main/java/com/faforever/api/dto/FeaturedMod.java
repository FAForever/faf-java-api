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
@Type(FeaturedMod.TYPE)
public class FeaturedMod implements ElideEntity {
  public static final String TYPE = "featuredMod";

  @Id
  @EqualsAndHashCode.Include
  private String id;
  private String description;
  private String displayName;
  private int order;
  private String gitBranch;
  private String gitUrl;
  private String bireusUrl;
  private String technicalName;
  private boolean visible;
}
