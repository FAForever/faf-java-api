package com.faforever.api.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SchemaVersionRepository extends JpaRepository<SchemaVersion, Integer> {

  @Query("select s.version from SchemaVersion s where s.installedRank = (select max (s.installedRank) from SchemaVersion s)")
  Optional<String> findMaxVersion();
}
