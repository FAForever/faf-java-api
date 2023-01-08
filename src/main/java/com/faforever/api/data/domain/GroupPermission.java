package com.faforever.api.data.domain;

import com.faforever.api.security.elide.permission.ReadUserGroupCheck;
import com.faforever.api.security.elide.permission.WriteUserGroupCheck;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.ReadPermission;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
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
@Include(name = "groupPermission")
@Setter
@ReadPermission(expression = ReadUserGroupCheck.EXPRESSION)
public class GroupPermission extends AbstractEntity<GroupPermission> implements GrantedAuthority {

  // Any changes to the permissions here need to be added to the dto class as well!
  public static final String ROLE_READ_AUDIT_LOG = "READ_AUDIT_LOG";
  public static final String ROLE_READ_TEAMKILL_REPORT = "READ_TEAMKILL_REPORT";
  public static final String ROLE_READ_ACCOUNT_PRIVATE_DETAILS = "READ_ACCOUNT_PRIVATE_DETAILS";
  public static final String ROLE_ADMIN_ACCOUNT_NOTE = "ADMIN_ACCOUNT_NOTE";
  public static final String ROLE_ADMIN_ACCOUNT_NAME_CHANGE = "ADMIN_ACCOUNT_NAME_CHANGE";
  public static final String ROLE_ADMIN_MODERATION_REPORT = "ADMIN_MODERATION_REPORT";
  public static final String ROLE_ADMIN_ACCOUNT_BAN = "ADMIN_ACCOUNT_BAN";
  public static final String ROLE_ADMIN_CLAN = "ADMIN_CLAN";
  public static final String ROLE_WRITE_COOP_MISSION = "WRITE_COOP_MISSION";
  public static final String ROLE_WRITE_AVATAR = "WRITE_AVATAR";
  public static final String ROLE_WRITE_MATCHMAKER_MAP = "WRITE_MATCHMAKER_MAP";
  public static final String ROLE_WRITE_EMAIL_DOMAIN_BAN = "WRITE_EMAIL_DOMAIN_BAN";
  public static final String ROLE_ADMIN_VOTE = "ADMIN_VOTE";
  public static final String ROLE_WRITE_USER_GROUP = "WRITE_USER_GROUP";
  public static final String ROLE_READ_USER_GROUP = "READ_USER_GROUP";
  public static final String ROLE_WRITE_TUTORIAL = "WRITE_TUTORIAL";
  public static final String ROLE_WRITE_NEWS_POST = "WRITE_NEWS_POST";
  public static final String ROLE_WRITE_OAUTH_CLIENT = "WRITE_OAUTH_CLIENT";
  public static final String ROLE_ADMIN_MAP = "ADMIN_MAP";
  public static final String ROLE_ADMIN_MOD = "ADMIN_MOD";
  public static final String ROLE_WRITE_MESSAGE = "WRITE_MESSAGE";

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
  @UpdatePermission(expression = WriteUserGroupCheck.EXPRESSION)
  public Set<UserGroup> getUserGroups() {
    return userGroups;
  }

  @Override
  @Transient
  public String getAuthority() {
    return "ROLE_" + technicalName;
  }
}
