package com.faforever.api.data.domain;

import lombok.EqualsAndHashCode;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.time.OffsetDateTime;

@MappedSuperclass
@Setter
@EqualsAndHashCode(of = "id")
public abstract class AbstractEntity {
  protected Integer id;
  protected OffsetDateTime createTime;
  protected OffsetDateTime updateTime;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  public Integer getId() {
    return id;
  }

  @Column(name = "create_time")
  public OffsetDateTime getCreateTime() {
    return createTime;
  }

  @Column(name = "update_time")
  public OffsetDateTime getUpdateTime() {
    return updateTime;
  }

  /**
   * Supplement method for @EqualsAndHashCode overriding the default lombok implementation
   */
  protected boolean canEqual(Object other) {
    return other instanceof AbstractEntity && this.getClass() == other.getClass();
  }
}
