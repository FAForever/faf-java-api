package com.faforever.api.featuredmods;

import com.faforever.api.data.domain.FeaturedMod;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeaturedModRepository extends JpaRepository<FeaturedMod, Integer> {
}
