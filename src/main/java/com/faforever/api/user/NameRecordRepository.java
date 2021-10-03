package com.faforever.api.user;

import com.faforever.api.data.domain.NameRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigInteger;
import java.util.Optional;

public interface NameRecordRepository extends JpaRepository<NameRecord, Integer> {
  @Query(value = "SELECT datediff(now(), change_time) FROM name_history WHERE user_id = :userId AND datediff(now(), change_time) <= :maximumDaysAgo ORDER BY change_time DESC LIMIT 1", nativeQuery = true)
  Optional<BigInteger> getDaysSinceLastNewRecord(@Param("userId") Integer userId, @Param("maximumDaysAgo") Integer maximumDaysAgo);

  @Query(value = "SELECT user_id FROM name_history WHERE previous_name = :name AND (now() - INTERVAL :months MONTH) < change_time ORDER BY change_time DESC LIMIT 1", nativeQuery = true)
  Optional<Integer> getLastUsernameOwnerWithinMonths(@Param("name") String name, @Param("months") Integer months);
}
