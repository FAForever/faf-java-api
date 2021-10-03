package com.faforever.api.data.domain;

import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.time.OffsetDateTime;

@SuppressWarnings("unchecked")
@MappedSuperclass
@EqualsAndHashCode(of = "id")
public abstract class AbstractEntity<T extends AbstractEntity<T>> {
  protected Integer id;
  protected OffsetDateTime createTime;
  protected OffsetDateTime updateTime;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  public Integer getId() {
    return id;
  }

  public T setId(Integer id) {
    this.id = id;
    return (T) this;
  }

  @Column(name = "create_time")
  public OffsetDateTime getCreateTime() {
    return createTime;
  }

  public T setCreateTime(OffsetDateTime createTime) {
    this.createTime = createTime;
    return (T) this;
  }

  @Column(name = "update_time")
  public OffsetDateTime getUpdateTime() {
    return updateTime;
  }

  public T setUpdateTime(OffsetDateTime updateTime) {
    this.updateTime = updateTime;
    return (T) this;
  }

  /**
   * Supplement method for @EqualsAndHashCode overriding the default lombok implementation
   */
  protected boolean canEqual(Object other) {
    return other instanceof AbstractEntity && this.getClass() == other.getClass();
  }
}
