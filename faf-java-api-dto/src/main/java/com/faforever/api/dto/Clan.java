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
@Type(Clan.TYPE)
public class Clan extends AbstractEntity {
  public static final String TYPE = "clan";

  private String name;
  private String tag;
  private String description;
  private String tagColor;
  private String websiteUrl;

  @Relationship("founder")
  private Player founder;

  @Relationship("leader")
  private Player leader;

  @Relationship("memberships")
  private List<ClanMembership> memberships;
}
