package com.faforever.api.voting;


import com.faforever.api.data.domain.VotingQuestion;
import org.springframework.data.jpa.repository.JpaRepository;


public interface VotingQuestionRepository extends JpaRepository<VotingQuestion, Integer> {

}

