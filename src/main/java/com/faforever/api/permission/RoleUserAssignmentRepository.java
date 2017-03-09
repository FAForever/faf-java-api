package com.faforever.api.permission;

import com.faforever.api.data.domain.RoleUserAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleUserAssignmentRepository extends JpaRepository<RoleUserAssignment, Integer> {
}
