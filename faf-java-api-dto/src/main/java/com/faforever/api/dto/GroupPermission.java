package com.faforever.api.dto;

import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@Type(GroupPermission.TYPE)
public class GroupPermission extends AbstractEntity {
  public static final String TYPE = "groupPermission";

  public static final String ROLE_READ_AUDIT_LOG = "ROLE_READ_AUDIT_LOG";
  public static final String ROLE_READ_TEAMKILL_REPORT = "ROLE_READ_TEAMKILL_REPORT";
  public static final String ROLE_READ_ACCOUNT_PRIVATE_DETAILS = "ROLE_READ_ACCOUNT_PRIVATE_DETAILS";
  public static final String ROLE_ADMIN_ACCOUNT_NOTE = "ROLE_ADMIN_ACCOUNT_NOTE";
  public static final String ROLE_ADMIN_MODERATION_REPORT = "ROLE_ADMIN_MODERATION_REPORT";
  public static final String ROLE_ADMIN_ACCOUNT_BAN = "ROLE_ADMIN_ACCOUNT_BAN";
  public static final String ROLE_ADMIN_CLAN = "ROLE_ADMIN_CLAN";
  public static final String ROLE_WRITE_MAP = "ROLE_WRITE_MAP";
  public static final String ROLE_WRITE_MOD = "ROLE_WRITE_MOD";
  public static final String ROLE_WRITE_COOP_MISSION = "ROLE_WRITE_COOP_MISSION";
  public static final String ROLE_WRITE_AVATAR = "ROLE_WRITE_AVATAR";
  public static final String ROLE_WRITE_MATCHMAKER_MAP = "ROLE_WRITE_MATCHMAKER_MAP";
  public static final String ROLE_WRITE_EMAIL_DOMAIN_BAN = "ROLE_WRITE_EMAIL_DOMAIN_BAN";
  public static final String ROLE_ADMIN_VOTE = "ROLE_ADMIN_VOTE";
  public static final String ROLE_WRITE_USER_GROUP = "ROLE_WRITE_USER_GROUP";
  public static final String ROLE_READ_USER_GROUP = "ROLE_READ_USER_GROUP";
  public static final String ROLE_WRITE_TUTORIAL = "ROLE_WRITE_TUTORIAL";
  public static final String ROLE_WRITE_NEWS_POST = "ROLE_WRITE_NEWS_POST";
  public static final String ROLE_WRITE_OAUTH_CLIENT = "ROLE_WRITE_OAUTH_CLIENT";
  public static final String ROLE_ADMIN_MAP = "ROLE_ADMIN_MAP";
  public static final String ROLE_ADMIN_MOD = "ROLE_ADMIN_MOD";
  public static final String ROLE_WRITE_MESSAGE = "ROLE_WRITE_MESSAGE";

  private String technicalName;
  private String nameKey;

  @Relationship("userGroups")
  private Set<UserGroup> userGroups;
}
