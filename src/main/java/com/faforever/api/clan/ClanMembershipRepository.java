package com.faforever.api.clan;

import com.faforever.api.data.domain.ClanMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClanMembershipRepository extends JpaRepository<ClanMembership, Integer> {

}
