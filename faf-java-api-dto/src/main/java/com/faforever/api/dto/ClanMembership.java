package com.faforever.api.dto;

import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@Type(ClanMembership.TYPE)
public class ClanMembership extends AbstractEntity {
  public static final String TYPE = "clanMembership";

  @Relationship("clan")
  private Clan clan;

  @Relationship("player")
  private Player player;
}
