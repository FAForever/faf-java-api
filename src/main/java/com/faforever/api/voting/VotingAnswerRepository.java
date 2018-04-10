package com.faforever.api.voting;

import com.faforever.api.data.domain.VotingAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VotingAnswerRepository extends JpaRepository<VotingAnswer, Integer> {
}
