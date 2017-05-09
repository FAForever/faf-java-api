package com.faforever.api.avatar;

import com.faforever.api.data.domain.AvatarAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AvatarAssignmentRepository extends JpaRepository<AvatarAssignment, Integer> {
}
