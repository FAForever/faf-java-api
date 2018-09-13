package com.faforever.api.voting;

import com.faforever.api.data.domain.Player;
import com.faforever.api.data.domain.Vote;
import com.faforever.api.data.domain.VotingSubject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.Set;

public interface VoteRepository extends JpaRepository<Vote, Integer> {
  Optional<Vote> findByPlayerAndVotingSubjectId(Player player, int votingSubjectId);

  Set<Vote> findByVotingSubject(VotingSubject votingSubject);
}
