package com.faforever.api.dto;

import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@Type(FeaturedModFile.TYPE)
public class FeaturedModFile {
  public static final String TYPE = "featuredModFile";

  @Id
  @EqualsAndHashCode.Include
  private String id;
  private String version;
  private String group;
  private String name;
  private String md5;
  private String url;
}
