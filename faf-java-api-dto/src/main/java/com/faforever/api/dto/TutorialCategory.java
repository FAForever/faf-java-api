package com.faforever.api.dto;

import com.faforever.api.elide.ElideEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Data
@Builder
@Type(TutorialCategory.TYPE)
public class TutorialCategory implements ElideEntity {
  public static final String TYPE = "tutorialCategory";

  @Id
  private String id;
  private String categoryKey;
  private String category;
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @Relationship("tutorials")
  @EqualsAndHashCode.Exclude
  @ToString.Exclude
  private List<Tutorial> tutorials;

}
