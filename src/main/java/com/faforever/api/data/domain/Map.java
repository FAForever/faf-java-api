package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import lombok.Setter;
import org.hibernate.annotations.Immutable;
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
import java.util.ArrayList;
import java.util.List;

@Entity
@Setter //Don't generate toString with lombok to avoid loops
@Table(name = "map")
@Include(rootLevel = true, type = "map")
@Immutable
public class Map {

  private int id;
  private String displayName;
  private String mapType;
  private String battleType;
  private Timestamp createTime;
  private Timestamp updateTime;
  private List<MapVersion> versions = new ArrayList<>();
  private Player author;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  public int getId() {
    return id;
  }

  @Column(name = "display_name", unique = true)
  @Size(max = 100)
  @NotNull
  public String getDisplayName() {
    return displayName;
  }

  @Column(name = "map_type")
  @NotNull
  public String getMapType() {
    return mapType;
  }

  @Column(name = "battle_type")
  @NotNull
  public String getBattleType() {
    return battleType;
  }

  @Column(name = "create_time")
  public Timestamp getCreateTime() {
    return createTime;
  }

  @Column(name = "update_time")
  public Timestamp getUpdateTime() {
    return updateTime;
  }

  @OneToMany(mappedBy = "map", cascade = CascadeType.ALL, orphanRemoval = true)
  @NotEmpty
  public List<MapVersion> getVersions() {
    return versions;
  }

  @ManyToOne
  @JoinColumn(name = "author")
  @NotNull
  public Player getAuthor() {
    return author;
  }

}
