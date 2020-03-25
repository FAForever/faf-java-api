package com.faforever.api.dto;


import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@Type(VotingAnswer.TYPE)
public class VotingAnswer extends AbstractEntity {
  public static final String TYPE = "votingAnswer";

  @Relationship("vote")
  private Vote vote;
  private Integer alternativeOrdinal;
  @Relationship("votingChoice")
  private VotingChoice votingChoice;
}
