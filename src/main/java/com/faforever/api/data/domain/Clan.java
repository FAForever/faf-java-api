package com.faforever.api.data.domain;

import com.faforever.api.config.elide.checks.IsClanLeader;
import com.faforever.api.data.validation.LeaderIsInClan;
import com.yahoo.elide.annotation.CreatePermission;
import com.yahoo.elide.annotation.DeletePermission;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.SharePermission;
import com.yahoo.elide.annotation.UpdatePermission;
import com.yahoo.elide.security.checks.prefab.Role;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.sql.Timestamp;
import java.util.List;

@Entity
@Table(name = "clan")
@Include(rootLevel = true, type = "clan")
@SharePermission(expression = IsClanLeader.EXPRESSION)
@DeletePermission(expression = IsClanLeader.EXPRESSION)
@CreatePermission(any = Role.ALL.class)
@Setter
@LeaderIsInClan
public class Clan {

  private int id;
  private Timestamp createTime;
  private Timestamp updateTime;
  private String name;
  private String tag;
  private Player founder;
  private Player leader;
  private String description;
  private String tagColor;
  private List<ClanMembership> memberships;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  public int getId() {
    return id;
  }

  @Column(name = "create_time")
  public Timestamp getCreateTime() {
    return createTime;
  }

  @Column(name = "update_time")
  public Timestamp getUpdateTime() {
    return updateTime;
  }

  @Column(name = "name")
  @NotNull
  @UpdatePermission(expression = IsClanLeader.EXPRESSION)
  public String getName() {
    return name;
  }

  @Column(name = "tag")
  @Size(max = 3)
  @NotNull
  @UpdatePermission(expression = IsClanLeader.EXPRESSION)
  public String getTag() {
    return tag;
  }

  @ManyToOne
  @JoinColumn(name = "founder_id")
  public Player getFounder() {
    return founder;
  }

  @ManyToOne
  @JoinColumn(name = "leader_id")
  @UpdatePermission(expression = IsClanLeader.EXPRESSION)
  public Player getLeader() {
    return leader;
  }

  @Column(name = "description")
  @UpdatePermission(expression = IsClanLeader.EXPRESSION)
  public String getDescription() {
    return description;
  }

  @Column(name = "tag_color")
  public String getTagColor() {
    return tagColor;
  }

  @OneToMany(mappedBy = "clan", cascade = CascadeType.ALL, orphanRemoval = true)
  // cascading is needed for Create & Delete
  @UpdatePermission(any = {Role.ALL.class}) // Permission is managed by ClanMembership class
  @NotEmpty(message = "At least the leader should be in the clan")
  public List<ClanMembership> getMemberships() {
    return this.memberships;
  }
}
