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
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.BatchSize;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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
@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class Login implements DefaultEntity, OwnableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  @EqualsAndHashCode.Include
  private Integer id;

  @Column(name = "create_time")
  private OffsetDateTime createTime;

  @Column(name = "update_time")
  private OffsetDateTime updateTime;

  @Column(name = "login")
  @EqualsAndHashCode.Include
  protected String login;

  @Column(name = "email")
  @ReadPermission(expression = IsEntityOwner.EXPRESSION + " OR " + ReadAccountPrivateDetailsCheck.EXPRESSION)
  private String email;

  @Column(name = "steamid")
  @ReadPermission(expression = IsEntityOwner.EXPRESSION + " OR " + ReadAccountPrivateDetailsCheck.EXPRESSION)
  private String steamId;

  @OneToMany
  @JoinTable(name = "unique_id_users",
    joinColumns = @JoinColumn(name = "user_id"),
    inverseJoinColumns = @JoinColumn(name = "uniqueid_hash", referencedColumnName = "hash")
  )
  @ReadPermission(expression = ReadAccountPrivateDetailsCheck.EXPRESSION)
  private Set<UniqueId> uniqueIds;

  @Column(name = "user_agent")
  private String userAgent;

  @OneToMany(mappedBy = "player", fetch = FetchType.EAGER)
  // Permission is managed by BanInfo class
  @UpdatePermission(expression = AdminAccountBanCheck.EXPRESSION)
  @BatchSize(size = 1000)
  private Set<BanInfo> bans = new HashSet<>(0);

  @OneToMany(mappedBy = "player", fetch = FetchType.EAGER)
  @UpdatePermission(expression = AdminAccountNoteCheck.EXPRESSION)
  @BatchSize(size = 1000)
  private Set<UserNote> userNotes = new HashSet<>(0);

  @ReadPermission(expression = IsEntityOwner.EXPRESSION + " OR " + ReadUserGroupCheck.EXPRESSION)
  @UpdatePermission(expression = Prefab.ALL)
  @ManyToMany(mappedBy = "members")
  @BatchSize(size = 1000)
  private Set<UserGroup> userGroups;

  @Column(name = "ip")
  @ReadPermission(expression = IsEntityOwner.EXPRESSION + " OR " + ReadAccountPrivateDetailsCheck.EXPRESSION)
  private String recentIpAddress;

  @Column(name = "last_login")
  @ReadPermission(expression = IsEntityOwner.EXPRESSION + " OR " + ReadAccountPrivateDetailsCheck.EXPRESSION)
  private OffsetDateTime lastLogin;

  @Transient
  public Set<BanInfo> getActiveBans() {
    return bans.stream().filter(ban -> ban.getBanStatus() == BanStatus.BANNED).collect(Collectors.toSet());
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

  @Override
  @Transient
  @JsonIgnore
  public Login getEntityOwner() {
    return this;
  }
}
