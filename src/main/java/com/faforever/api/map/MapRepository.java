package com.faforever.api.map;

import com.faforever.api.data.domain.Map;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MapRepository extends JpaRepository<Map, Integer> {

  // https://docs.spring.io/spring-data/jpa/docs/current/reference/html/
  Optional<Map> findOneByDisplayName(String displayName);
}
