package com.faforever.api.voting;

import com.faforever.api.data.domain.VotingChoice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VotingChoiceRepository extends JpaRepository<VotingChoice, Integer> {
}
