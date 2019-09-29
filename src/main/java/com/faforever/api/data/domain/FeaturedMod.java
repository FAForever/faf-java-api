package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.ComputedAttribute;
import com.yahoo.elide.annotation.Include;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "game_featuredMods")
@Include(rootLevel = true, type = FeaturedMod.TYPE_NAME)
@Getter
@Setter
@EntityListeners(FeaturedModEnricher.class)
public class FeaturedMod {
  public static final String TYPE_NAME = "featuredMod";

  @Id
  @Column(name = "id")
  private int id;

  @Column(name = "gamemod")
  private String technicalName;

  @Column(name = "description")
  private String description;

  @Column(name = "name")
  private String displayName;

  @Column(name = "publish")
  private boolean visible;

  @Column(name = "\"order\"")
  private int order;

  @Column(name = "git_url")
  private String gitUrl;

  @Column(name = "git_branch")
  private String gitBranch;

  @Getter(AccessLevel.NONE) // for type Boolean Lombok generates get* method, not is*
  @Column(name = "allow_override")
  private Boolean allowOverride;

  @Column(name = "file_extension")
  private String fileExtension;

  @Column(name = "deployment_webhook ")
  private String deploymentWebhook;

  // Enriched by FeaturedModEnricher
  @Transient
  @ComputedAttribute
  private String bireusUrl;

  public Boolean isAllowOverride() {
    return allowOverride;
  }
}
