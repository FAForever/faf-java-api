package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "login_role")
@Include(rootLevel = true, type = "role")
@Setter
public class Role {
  private int id;
  private OffsetDateTime createTime;
  private OffsetDateTime updateTime;
  private String name;
  private List<RolePermissionAssignment> permissionAssignments;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  public int getId() {
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

  @Column(name = "name")
  public String getName() {
    return name;
  }

  @OneToMany(mappedBy = "role", cascade = CascadeType.ALL)
  @UpdatePermission(any = {com.yahoo.elide.security.checks.prefab.Role.ALL.class})
  // Permission is managed by RolePermissionAssignment class
  public List<RolePermissionAssignment> getPermissionAssignments() {
    return permissionAssignments;
  }
}
