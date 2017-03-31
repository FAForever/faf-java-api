package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "game_featuredMods")
@Include(rootLevel = true, type = "featuredMod")
@Setter
public class FeaturedMod {
  private int id;
  private String technicalName;
  private String description;
  private String displayName;
  private boolean visible;
  private int order;
  private String gitUrl;
  private String gitBranch;

  @Id
  @Column(name = "id")
  public int getId() {
    return id;
  }

  @Column(name = "gamemod")
  public String getTechnicalName() {
    return technicalName;
  }

  @Column(name = "description")
  public String getDescription() {
    return description;
  }

  @Column(name = "name")
  public String getDisplayName() {
    return displayName;
  }

  @Column(name = "publish")
  public boolean isVisible() {
    return visible;
  }

  @Column(name = "\"order\"")
  public int getOrder() {
    return order;
  }

  @Column(name = "git_url")
  public String getGitUrl() {
    return gitUrl;
  }

  @Column(name = "git_branch")
  public String getGitBranch() {
    return gitBranch;
  }
}
