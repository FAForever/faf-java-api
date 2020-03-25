package com.faforever.api.dto;

import com.faforever.api.elide.ElideEntity;
import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Type(MeResult.TYPE_NAME)
@Data
@Builder
public class MeResult implements ElideEntity {

  public static final String TYPE_NAME = "me";

  @Id
  private String userId;
  private String userName;
  private String email;
  private Clan clan;
  private Set<String> groups;
  private Set<String> permissions;

  @Override
  public String getId() {
    return userId;
  }

  public static class Clan {
    private Integer id;
    private Integer membershipId;
    private String tag;
    private String name;
  }
}
