package com.faforever.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@Type(UserGroup.TYPE)
public class UserGroup extends AbstractEntity {
  public static final String TYPE = "userGroup";

  private String technicalName;
  private String nameKey;
  private boolean public_;
  @Relationship("members")
  @JsonIgnore
  private Set<Player> members;
  @Relationship("permissions")
  private Set<GroupPermission> permissions;
}
