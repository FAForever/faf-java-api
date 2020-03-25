package com.faforever.api.dto;

import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@Type(VotingChoice.TYPE)
public class VotingChoice extends AbstractEntity {
  public static final String TYPE = "votingChoice";

  private String choiceTextKey;
  private String choiceText;
  private String descriptionKey;
  private String description;
  private Integer numberOfAnswers;
  private Integer ordinal;
  @Relationship("votingQuestion")
  private VotingQuestion votingQuestion;
}
