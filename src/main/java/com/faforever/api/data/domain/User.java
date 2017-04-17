package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;

@Entity
@Table(name = "login")
@Setter
public class User extends Login {

  private String password;
  private List<RoleUserAssignment> roleAssignments;

  @Column(name = "password")
  public String getPassword() {
    return password;
  }

  public boolean hasPermission(String permission) {
    return roleAssignments.stream()
        .flatMap(roleUserAssignment -> roleUserAssignment.getRole().getPermissionAssignments().stream())
        .anyMatch(rolePermissionAssignment -> permission.equals(rolePermissionAssignment.getPermission().getName()));
  }

  @OneToMany(mappedBy = "user")
  @UpdatePermission(any = {com.yahoo.elide.security.checks.prefab.Role.ALL.class})
  // Permission is managed by RoleUserAssignment class
  public List<RoleUserAssignment> getRoleAssignments() {
    return roleAssignments;
  }
}
