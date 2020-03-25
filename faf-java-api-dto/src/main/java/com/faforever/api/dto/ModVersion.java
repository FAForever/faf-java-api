package com.faforever.api.dto;

import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.net.URL;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@Type(ModVersion.TYPE)
public class ModVersion extends AbstractEntity {
  public static final String TYPE = "modVersion";

  private String uid;
  private ModType type;
  private String description;
  private Integer version;
  private String filename;
  private String icon;
  private boolean ranked;
  private boolean hidden;
  private URL thumbnailUrl;
  private URL downloadUrl;

  @Relationship("mod")
  private Mod mod;
}
