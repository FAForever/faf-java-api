package com.faforever.api.achievements;

import com.faforever.api.data.domain.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AchievementRepository extends JpaRepository<Achievement, String> {

}
