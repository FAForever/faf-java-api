package com.faforever.api.data.domain;

import com.faforever.api.config.elide.ElideConfig;
import com.yahoo.elide.annotation.*;
import com.yahoo.elide.security.checks.prefab.Role;
import lombok.Data;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.sql.Timestamp;
import java.util.List;

@Entity
@Table(name = "clan")
@Include(rootLevel = true, type = "clan")
@UpdatePermission(expression = ElideConfig.USER_IS_CLAN_LEADER)
@Data
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
  public String getName() {
    return name;
  }

  @Column(name = "tag")
  @Size(max = 3)
  @NotNull
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
  public Player getLeader() {
    return leader;
  }

  @Column(name = "description")
  public String getDescription() {
    return description;
  }

  @Column(name = "tag_color")
  public String getTagColor() {
    return tagColor;
  }

  @OneToMany(mappedBy = "clan")
  public List<ClanMembership> getMemberships() {
    return this.memberships;
  }
}
