package com.faforever.api.dto;

import com.faforever.api.elide.ElideEntity;
import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@Type(Mod.TYPE)
public class Mod implements ElideEntity {
  public static final String TYPE = "mod";

  @Id
  @EqualsAndHashCode.Include
  private String id;
  private String displayName;
  private String author;
  private OffsetDateTime createTime;

  @Relationship("uploader")
  private Player uploader;

  @Relationship("versions")
  @ToString.Exclude
  private List<ModVersion> versions;

  @Relationship("latestVersion")
  @ToString.Exclude
  private ModVersion latestVersion;
}

