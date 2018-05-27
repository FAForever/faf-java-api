package com.faforever.api.rating;

import com.faforever.api.data.domain.GlobalRating;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GlobalRatingRepository extends JpaRepository<GlobalRating, Integer> {

}
