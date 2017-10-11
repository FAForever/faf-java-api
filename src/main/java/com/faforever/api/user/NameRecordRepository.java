package com.faforever.api.user;

import com.faforever.api.data.domain.NameRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface NameRecordRepository extends JpaRepository<NameRecord, Integer> {
  @Query(value = "SELECT datediff(now(), change_time) FROM name_history WHERE user_id = :userId AND datediff(now(), change_time) <= :maximumDaysAgo ORDER BY change_time DESC LIMIT 1", nativeQuery = true)
  Optional<Integer> getDaysSinceLastNewRecord(@Param("userId") Integer userId, @Param("maximumDaysAgo") Integer maximumDaysAgo);
}
