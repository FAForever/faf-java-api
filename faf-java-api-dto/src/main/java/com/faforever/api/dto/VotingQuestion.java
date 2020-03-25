package com.faforever.api.dto;

import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@Type(VotingQuestion.TYPE)
public class VotingQuestion extends AbstractEntity {
  public static final String TYPE = "votingQuestion";

  private int numberOfAnswers;
  private String question;
  private String description;
  private String questionKey;
  private String descriptionKey;
  private Integer maxAnswers;
  private Integer ordinal;
  private Boolean alternativeQuestion;
  @Relationship("votingSubject")
  private VotingSubject votingSubject;
  @Relationship("winners")
  private List<VotingChoice> winners;
  @Relationship("votingChoices")
  private List<VotingChoice> votingChoices;

}
