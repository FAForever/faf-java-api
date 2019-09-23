package com.faforever.api.data.domain;

import com.faforever.api.data.checks.permission.IsModerator;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.ReadPermission;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Set;

/**
 * <b>Naming conventions on permission roles</b>
 * <p>
 * General structure: ROLE_{ACTION}_{AREA}
 * <ul>
 * <li>{AREA} defines a set of use cases (i.e. BAN)
 * <li>{ACTION} being WRITE, READ or ADMIN
 * <br>WRITE grants permission to create / add / upload new data
 * <br>READ grants global read permissions
 * <br>ADMIN refer to read and write permissions in global context regardless of the ownership
 * </ul>
 * <p>
 * For read and write permissions of data personally owned by user see {@link OwnableEntity}
 */
@Entity
@Table(name = "group_permission")
@Include(rootLevel = true, type = "groupPermission")
@Setter
@ReadPermission(expression = IsModerator.EXPRESSION)
public class GroupPermission extends AbstractEntity implements GrantedAuthority {

  // Any changes to the permissions here need to be added to the dto class as well!
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
  public static final String ROLE_WRITE_MATCHMAKER_POOL = "ROLE_WRITE_MATCHMAKER_POOL";
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
  private Set<UserGroup> userGroups;

  @Column(name = "technical_name")
  public String getTechnicalName() {
    return technicalName;
  }

  @Column(name = "name_key")
  public String getNameKey() {
    return nameKey;
  }

  @ManyToMany(mappedBy = "permissions")
  public Set<UserGroup> getUserGroups() {
    return userGroups;
  }

  @Override
  @Transient
  public String getAuthority() {
    return "ROLE_" + technicalName;
  }
}
