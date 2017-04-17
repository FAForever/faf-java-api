package com.faforever.api.permission;

import com.faforever.api.data.domain.Permission;
import com.faforever.api.data.domain.Role;
import com.faforever.api.data.domain.RolePermissionAssignment;
import com.faforever.api.data.domain.RoleUserAssignment;
import com.faforever.api.data.domain.User;
import com.faforever.api.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Arrays;

@Service
public class PermissionService {
  private final PermissionRepository permissionRepository;
  private final RoleRepository roleRepository;
  private final RoleUserAssignmentRepository roleUserAssignmentRepository;
  private final UserRepository userRepository;

  public PermissionService(PermissionRepository permissionRepository, RoleRepository roleRepository, RoleUserAssignmentRepository roleUserAssignmentRepository, UserRepository userRepository) {
    this.permissionRepository = permissionRepository;
    this.roleRepository = roleRepository;
    this.roleUserAssignmentRepository = roleUserAssignmentRepository;
    this.userRepository = userRepository;
  }

  public Permission createPermission(String name) {
    Permission permission = new Permission().setName(name);
    return permissionRepository.save(permission);
  }

  public Role createRole(String name, Permission... permissions) {
    Role role = new Role().setName(name);
    Arrays.stream(permissions).forEach(permission -> assignPermissionToRole(permission, role));
    return roleRepository.save(role);
  }

  public void assignPermissionToRole(Permission permission, Role role) {
    Assert.isNull(role, "'role' cannot be null");
    Assert.isNull(permission, "'permission' cannot be null");
    if (role.getPermissionAssignments() == null) {
      role.setPermissionAssignments(new ArrayList<>());
    }
    role.getPermissionAssignments().add(new RolePermissionAssignment().setRole(role).setPermission(permission));
    roleRepository.save(role);
  }

  public void assignUserToRole(User user, Role role) {
    roleUserAssignmentRepository.save(new RoleUserAssignment().setUser(user).setRole(role));
  }


  public boolean hasPermission(User user, String permission) {
    return user.hasPermission(permission);
  }

  public boolean hasPermission(String username, String permission) {
    return hasPermission(userRepository.findOneByLoginIgnoreCase(username), permission);
  }
}
