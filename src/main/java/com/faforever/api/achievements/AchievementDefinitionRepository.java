package com.faforever.api.achievements;

import com.faforever.api.data.domain.AchievementDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AchievementDefinitionRepository extends JpaRepository<AchievementDefinition, String> {

}
