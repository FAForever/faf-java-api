package com.faforever.api.dto;

import com.faforever.api.elide.ElideEntity;
import com.github.jasminb.jsonapi.annotations.Id;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.OffsetDateTime;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@SuperBuilder
public abstract class AbstractEntity implements ElideEntity {
  @Id
  @EqualsAndHashCode.Include
  protected String id;
  protected OffsetDateTime createTime;
  protected OffsetDateTime updateTime;

  /**
   * Supplement method for @EqualsAndHashCode overriding the default lombok implementation
   */
  protected boolean canEqual(Object other) {
    return other instanceof AbstractEntity && this.getClass() == other.getClass();
  }
}
