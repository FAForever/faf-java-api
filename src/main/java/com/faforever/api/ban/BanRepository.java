package com.faforever.api.ban;

import com.faforever.api.data.domain.BanInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BanRepository extends JpaRepository<BanInfo, Integer> {
}
