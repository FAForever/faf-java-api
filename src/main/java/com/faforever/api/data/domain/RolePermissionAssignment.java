package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "login_role_permission")
@Include(rootLevel = true, type = "rolePermissionAssignment")
@Setter
public class RolePermissionAssignment {

  private int id;
  private OffsetDateTime createTime;
  private OffsetDateTime updateTime;
  private Role role;
  private Permission permission;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  public int getId() {
    return id;
  }

  @ManyToOne
  @JoinColumn(name = "role_id")
  public Role getRole() {
    return role;
  }

  @ManyToOne
  @JoinColumn(name = "permission_id")
  public Permission getPermission() {
    return permission;
  }

  @Column(name = "create_time")
  public OffsetDateTime getCreateTime() {
    return createTime;
  }

  @Column(name = "update_time")
  public OffsetDateTime getUpdateTime() {
    return updateTime;
  }
}
