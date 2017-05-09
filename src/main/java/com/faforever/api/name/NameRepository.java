package com.faforever.api.name;

import com.faforever.api.data.domain.NameRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NameRepository extends JpaRepository<NameRecord, Integer> {
}
