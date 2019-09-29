package com.faforever.api.data.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.time.OffsetDateTime;

@MappedSuperclass
@Getter
@Setter
@EqualsAndHashCode(of = "id")
public abstract class AbstractEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  protected int id;

  @Column(name = "create_time")
  protected OffsetDateTime createTime;

  @Column(name = "update_time")
  protected OffsetDateTime updateTime;

  /**
   * Supplement method for @EqualsAndHashCode overriding the default lombok implementation
   */
  protected boolean canEqual(Object other) {
    return other instanceof AbstractEntity && this.getClass() == other.getClass();
  }
}
