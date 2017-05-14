package com.faforever.api.hardwareinformation;

import com.faforever.api.data.domain.HardwareInformation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HardwareInformationRepository extends JpaRepository<HardwareInformation, Integer> {
}
