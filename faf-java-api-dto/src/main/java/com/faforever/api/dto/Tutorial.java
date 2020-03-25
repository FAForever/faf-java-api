package com.faforever.api.dto;

import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@Type(Tutorial.TYPE)
public class Tutorial extends AbstractEntity {
  public static final String TYPE = "tutorial";

  private String descriptionKey;
  private String description;
  private String titleKey;
  private String title;
  @Relationship("category")
  private TutorialCategory category;
  private String image;
  private String imageUrl;
  private int ordinal;
  private boolean launchable;
  private String technicalName;
  @Relationship("mapVersion")
  private MapVersion mapVersion;
}
