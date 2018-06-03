package com.faforever.api.voting;

import com.faforever.api.data.domain.VotingSubject;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VotingSubjectRepository extends JpaRepository<VotingSubject, Integer> {
}
