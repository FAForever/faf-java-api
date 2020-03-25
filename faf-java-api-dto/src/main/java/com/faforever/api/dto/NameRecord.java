package com.faforever.api.dto;

import com.faforever.api.elide.ElideEntity;
import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.OffsetDateTime;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@Type(NameRecord.TYPE)
public class NameRecord implements ElideEntity {
  public static final String TYPE = "nameRecord";

  @Id
  @EqualsAndHashCode.Include
  private String id;
  private OffsetDateTime changeTime;
  @Relationship("player")
  private Player player;
  private String name;
}
