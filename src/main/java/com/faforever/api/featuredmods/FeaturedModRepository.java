package com.faforever.api.featuredmods;

import com.faforever.api.data.domain.FeaturedMod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FeaturedModRepository extends JpaRepository<FeaturedMod, Integer> {
  Optional<FeaturedMod> findByGitUrlAndGitBranch(String url, String branch);
}
