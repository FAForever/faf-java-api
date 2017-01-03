package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "clan_list")
@Include(rootLevel = true, type = "clan")
public class Clan {

  private int clanId;
  private Timestamp createDate;
  private int status;
  private String clanName;
  private String clanTag;
  private Player clanFounder;
  private Player clanLeader;
  private String clanDesc;
  private String clanTagColor;
  private List<ClanMembership> memberships;

  @Id
  @Column(name = "clan_id")
  public int getClanId() {
    return clanId;
  }

  public void setClanId(int clanId) {
    this.clanId = clanId;
  }

  @Basic
  @Column(name = "create_date")
  public Timestamp getCreateDate() {
    return createDate;
  }

  public void setCreateDate(Timestamp createDate) {
    this.createDate = createDate;
  }

  @Basic
  @Column(name = "status")
  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  @Basic
  @Column(name = "clan_name")
  public String getClanName() {
    return clanName;
  }

  public void setClanName(String clanName) {
    this.clanName = clanName;
  }

  @Basic
  @Column(name = "clan_tag")
  public String getClanTag() {
    return clanTag;
  }

  public void setClanTag(String clanTag) {
    this.clanTag = clanTag;
  }

  @ManyToOne
  @JoinColumn(name = "clan_founder_id")
  public Player getClanFounder() {
    return clanFounder;
  }

  public void setClanFounder(Player clanFounderId) {
    this.clanFounder = clanFounderId;
  }

  @ManyToOne
  @JoinColumn(name = "clan_leader_id")
  public Player getClanLeader() {
    return clanLeader;
  }

  public void setClanLeader(Player clanLeaderId) {
    this.clanLeader = clanLeaderId;
  }

  @Basic
  @Column(name = "clan_desc")
  public String getClanDesc() {
    return clanDesc;
  }

  public void setClanDesc(String clanDesc) {
    this.clanDesc = clanDesc;
  }

  @Basic
  @Column(name = "clan_tag_color")
  public String getClanTagColor() {
    return clanTagColor;
  }

  public void setClanTagColor(String clanTagColor) {
    this.clanTagColor = clanTagColor;
  }

  @OneToMany(mappedBy = "clan")
  public List<ClanMembership> getMemberships() {
    return memberships;
  }

  public void setMemberships(List<ClanMembership> memberships) {
    this.memberships = memberships;
  }

  @Override
  public int hashCode() {
    return Objects.hash(clanId, createDate, status, clanName, clanTag, clanFounder, clanLeader, clanDesc, clanTagColor);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Clan clan = (Clan) o;
    return clanId == clan.clanId &&
        status == clan.status &&
        clanFounder == clan.clanFounder &&
        clanLeader == clan.clanLeader &&
        Objects.equals(createDate, clan.createDate) &&
        Objects.equals(clanName, clan.clanName) &&
        Objects.equals(clanTag, clan.clanTag) &&
        Objects.equals(clanDesc, clan.clanDesc) &&
        Objects.equals(clanTagColor, clan.clanTagColor);
  }
}
