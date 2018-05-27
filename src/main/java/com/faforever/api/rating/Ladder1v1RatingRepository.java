package com.faforever.api.rating;

import com.faforever.api.data.domain.Ladder1v1Rating;
import org.springframework.data.jpa.repository.JpaRepository;

public interface Ladder1v1RatingRepository extends JpaRepository<Ladder1v1Rating, Integer> {
}
