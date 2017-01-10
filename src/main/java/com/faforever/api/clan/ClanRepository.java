package com.faforever.api.clan;

import com.faforever.api.data.domain.Clan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClanRepository extends JpaRepository<Clan, Integer> {

}
