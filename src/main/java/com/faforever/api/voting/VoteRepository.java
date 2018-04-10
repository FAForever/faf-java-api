package com.faforever.api.voting;

import com.faforever.api.data.domain.Player;
import com.faforever.api.data.domain.Vote;
import com.faforever.api.data.domain.VotingSubject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VoteRepository extends JpaRepository<Vote, Integer> {
  Optional<Vote> findByPlayerAndVotingSubjectId(Player player, int votingSubjectId);

  List<Vote> findByVotingSubject(VotingSubject votingSubject);
}
