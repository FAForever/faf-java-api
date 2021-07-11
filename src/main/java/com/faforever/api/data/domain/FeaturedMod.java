package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "game_featuredMods")
@Include(name = FeaturedMod.TYPE_NAME)
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
public class FeaturedMod {
  public static final String TYPE_NAME = "featuredMod";

  @Id
  @Column(name = "id")
  @EqualsAndHashCode.Include
  private int id;

  @Column(name = "gamemod")
  @EqualsAndHashCode.Include
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

  @Column(name = "allow_override")
  private Boolean allowOverride;

  @Column(name = "file_extension")
  private String fileExtension;

  @Column(name = "deployment_webhook ")
  private String deploymentWebhook;
}
