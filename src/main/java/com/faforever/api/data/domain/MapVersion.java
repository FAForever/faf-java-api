package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import lombok.Setter;

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
import java.sql.Timestamp;
import java.util.List;

@Entity
@Setter
@Table(name = "map_version")
@Include(rootLevel = true, type = "map_version")
public class MapVersion {

  private int id;
  private String description;
  private Integer maxPlayers;
  private int width;
  private int height;
  private int version;
  private String filename;
  private boolean ranked;
  private boolean hidden;
  private Timestamp createTime;
  private Timestamp updateTime;
  private Map map;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  public int getId() {
    return id;
  }

  @Column(name = "description")
  public String getDescription() {
    return description;
  }

  @Column(name = "max_players")
  @NotNull
  public Integer getMaxPlayers() {
    return maxPlayers;
  }

  @Column(name = "width")
  @NotNull
  public int getWidth() {
    return width;
  }

  @Column(name = "height")
  @NotNull
  public int getHeight() {
    return height;
  }

  @Column(name = "version")
  @NotNull
  public int getVersion() {
    return version;
  }

  @Column(name = "filename")
  @NotNull
  public String getFilename() {
    return filename;
  }

  @Column(name = "ranked")
  @NotNull
  public boolean getRanked() {
    return ranked;
  }

  @Column(name = "hidden")
  @NotNull
  public boolean getHidden() {
    return hidden;
  }

  @Column(name = "create_time")
  public Timestamp getCreateTime() {
    return createTime;
  }

  @Column(name = "update_time")
  public Timestamp getUpdateTime() {
    return updateTime;
  }

  @ManyToOne
  @JoinColumn(name = "map_id")
  @NotNull
  public Map getMap() {
    return this.map;
  }
}
