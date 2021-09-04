package com.faforever.api.data.domain;

import com.faforever.api.data.checks.IsEntityOwner;
import com.faforever.api.data.checks.Prefab;
import com.faforever.api.security.elide.permission.AdminAccountBanCheck;
import com.faforever.api.security.elide.permission.AdminAccountNoteCheck;
import com.faforever.api.security.elide.permission.ReadAccountPrivateDetailsCheck;
import com.faforever.api.security.elide.permission.ReadUserGroupCheck;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.yahoo.elide.annotation.ReadPermission;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@MappedSuperclass
@Setter
public abstract class Login extends AbstractEntity<Login> implements OwnableEntity {

  private String login;
  private String email;
  private String steamId;
  private String gogId;
  private String userAgent;
  private Set<BanInfo> bans;
  private Set<UserNote> userNotes;
  private Set<UserGroup> userGroups;
  private String recentIpAddress;
  private OffsetDateTime lastLogin;
  private Set<UniqueId> uniqueIds;


  public Login() {
    this.bans = new HashSet<>(0);
    this.userNotes = new HashSet<>(0);
  }

  @Column(name = "login")
  public String getLogin() {
    return login;
  }

  @Column(name = "email")
  @ReadPermission(expression = IsEntityOwner.EXPRESSION + " OR " + ReadAccountPrivateDetailsCheck.EXPRESSION)
  public String getEmail() {
    return email;
  }

  @Column(name = "steamid")
  @ReadPermission(expression = IsEntityOwner.EXPRESSION + " OR " + ReadAccountPrivateDetailsCheck.EXPRESSION)
  public String getSteamId() {
    return steamId;
  }

  @Column(name = "gog_id")
  @ReadPermission(expression = IsEntityOwner.EXPRESSION + " OR " + ReadAccountPrivateDetailsCheck.EXPRESSION)
  public String getGogId() {
    return gogId;
  }

  @Column(name = "ip")
  @ReadPermission(expression = IsEntityOwner.EXPRESSION + " OR " + ReadAccountPrivateDetailsCheck.EXPRESSION)
  public String getRecentIpAddress() {
    return recentIpAddress;
  }

  @Column(name = "last_login")
  @ReadPermission(expression = IsEntityOwner.EXPRESSION + " OR " + ReadAccountPrivateDetailsCheck.EXPRESSION)
  public OffsetDateTime getLastLogin() {
    return lastLogin;
  }

  @Column(name = "user_agent")
  public String getUserAgent() {
    return userAgent;
  }

  @OneToMany(mappedBy = "player", fetch = FetchType.EAGER)
  // Permission is managed by BanInfo class
  @UpdatePermission(expression = AdminAccountBanCheck.EXPRESSION)
  @BatchSize(size = 1000)
  public Set<BanInfo> getBans() {
    return this.bans;
  }

  @OneToMany(mappedBy = "player", fetch = FetchType.EAGER)
  @UpdatePermission(expression = AdminAccountNoteCheck.EXPRESSION)
  @BatchSize(size = 1000)
  public Set<UserNote> getUserNotes() {
    return this.userNotes;
  }

  @Transient
  public Set<BanInfo> getActiveBans() {
    return getBans().stream().filter(ban -> ban.getBanStatus() == BanStatus.BANNED).collect(Collectors.toSet());
  }

  @Transient
  public Optional<BanInfo> getActiveBanOf(BanLevel banLevel) {
    return getActiveBans().stream()
      .filter(ban -> ban.getLevel() == banLevel)
      .findFirst();
  }

  @Transient
  public boolean isGlobalBanned() {
    return getActiveBans().stream().anyMatch(ban -> ban.getLevel() == BanLevel.GLOBAL);
  }

  @ReadPermission(expression = IsEntityOwner.EXPRESSION + " OR " + ReadUserGroupCheck.EXPRESSION)
  @UpdatePermission(expression = Prefab.ALL)
  @ManyToMany(mappedBy = "members")
  @BatchSize(size = 1000)
  public Set<UserGroup> getUserGroups() {
    return userGroups;
  }

  @OneToMany
  @JoinTable(name = "unique_id_users",
    joinColumns = @JoinColumn(name = "user_id"),
    inverseJoinColumns = @JoinColumn(name = "uniqueid_hash", referencedColumnName = "hash")
  )
  @ReadPermission(expression = ReadAccountPrivateDetailsCheck.EXPRESSION)
  public Set<UniqueId> getUniqueIds() {
    return uniqueIds;
  }

  @Override
  @Transient
  @JsonIgnore
  public Login getEntityOwner() {
    return this;
  }
}
