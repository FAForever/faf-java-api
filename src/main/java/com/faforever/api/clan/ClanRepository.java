package com.faforever.api.clan;

import com.faforever.api.data.domain.Clan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClanRepository extends JpaRepository<Clan, Integer> {

}
