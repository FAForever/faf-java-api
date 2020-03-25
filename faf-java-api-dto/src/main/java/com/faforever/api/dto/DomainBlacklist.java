package com.faforever.api.dto;

import com.faforever.api.elide.ElideEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@Type(DomainBlacklist.TYPE)
@RestrictedVisibility("IsModerator")
public class DomainBlacklist implements ElideEntity {
  public static final String TYPE = "domainBlacklist";

  @Id
  @EqualsAndHashCode.Include
  String domain;

  @Override
  @JsonIgnore
  public String getId() {
    return domain;
  }
}
