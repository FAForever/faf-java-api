package com.faforever.api.permission;

import com.faforever.api.data.domain.RolePermissionAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RolePermissionsAssingmentRepository extends JpaRepository<RolePermissionAssignment, Integer> {
}
