package com.faforever.api.data.domain;

import com.faforever.api.data.checks.IsEntityOwner;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;

@Getter
@Setter
@MappedSuperclass
public class Review extends AbstractEntity implements OwnableEntity {

  @Column(name = "text")
  @UpdatePermission(expression = IsEntityOwner.EXPRESSION)
  private String text;

  @Column(name = "score")
  @DecimalMin("1")
  @DecimalMax("5")
  @UpdatePermission(expression = IsEntityOwner.EXPRESSION)
  private Byte score;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  @UpdatePermission(expression = "Prefab.Role.All and Prefab.Common.UpdateOnCreate")
  private Player player;

  @Override
  @Transient
  public Login getEntityOwner() {
    return getPlayer();
  }
}
