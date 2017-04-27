package com.faforever.api.data.domain;

import com.faforever.api.data.checks.IsReviewOwner;
import com.yahoo.elide.annotation.CreatePermission;
import com.yahoo.elide.annotation.DeletePermission;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.EqualsAndHashCode;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import java.sql.Timestamp;

@Setter
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "review")
@Inheritance(strategy = InheritanceType.JOINED)
@CreatePermission(expression = "Prefab.Role.All")
@DeletePermission(expression = IsReviewOwner.EXPRESSION)
public class Review {
  private Integer id;
  private String text;
  private Byte score;
  private Timestamp createTime;
  private Timestamp updateTime;
  private Player player;

  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Integer getId() {
    return id;
  }

  @Column(name = "text")
  @UpdatePermission(expression = IsReviewOwner.EXPRESSION)
  public String getText() {
    return text;
  }

  @Column(name = "score")
  @DecimalMin("1")
  @DecimalMax("5")
  @UpdatePermission(expression = IsReviewOwner.EXPRESSION)
  public Byte getScore() {
    return score;
  }

  @Column(name = "create_time")
  public Timestamp getCreateTime() {
    return createTime;
  }

  @Column(name = "update_time")
  public Timestamp getUpdateTime() {
    return updateTime;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  @UpdatePermission(expression = "Prefab.Role.All and Prefab.Common.UpdateOnCreate")
  public Player getPlayer() {
    return player;
  }
}
