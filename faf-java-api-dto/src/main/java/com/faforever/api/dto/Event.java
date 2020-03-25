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
@Type(Event.TYPE)
public class Event implements ElideEntity {
  public static final String TYPE = "event";

  @Id
  @EqualsAndHashCode.Include
  private String id;
  private String name;
  private String imageUrl;
  private Type type;

  public enum Type {
    NUMERIC, TIME
  }
}
