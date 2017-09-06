package com.faforever.api.featuredmods;

import com.faforever.api.data.domain.FeaturedMod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FeaturedModRepository extends JpaRepository<FeaturedMod, Integer> {
  Optional<FeaturedMod> findOneByTechnicalName(String name);

  Optional<FeaturedMod> findByGitUrlAndGitBranch(String gitUrl, String gitBranch);
}
