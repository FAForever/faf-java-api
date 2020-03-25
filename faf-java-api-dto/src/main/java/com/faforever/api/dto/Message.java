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
@Type(Message.TYPE)
public class Message implements ElideEntity {
  public static final String TYPE = "message";

  @Id
  @EqualsAndHashCode.Include
  private String id;
  private String key;
  private String language;
  private String region;
  private String value;
}
