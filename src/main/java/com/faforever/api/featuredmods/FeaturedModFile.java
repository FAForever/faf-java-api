package com.faforever.api.featuredmods;

import lombok.Setter;
import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.Transient;

@Setter
@Immutable
@Entity
@EntityListeners(FeaturedModFileEnricher.class)
public class FeaturedModFile {

  private int id;
  private String group;
  private String md5;
  private String name;
  private short version;
  private String url;
  private String folderName;

  @Id
  @Column(name = "id")
  public int getId() {
    return id;
  }

  @Column(name = "group")
  public String getGroup() {
    return group;
  }

  @Column(name = "md5")
  public String getMd5() {
    return md5;
  }

  @Column(name = "name")
  public String getName() {
    return name;
  }

  @Column(name = "version")
  public short getVersion() {
    return version;
  }

  @Transient
  // Enriched by FeaturedModFileEnricher
  public String getUrl() {
    return url;
  }

  /**
   * Returns the name of the folder in which the file resides (e.g. {@code updates_faf_files}). Used by the
   * FeaturedModFileEnricher.
   */
  @Column(name = "folderName")
  public String getFolderName() {
    return folderName;
  }
}
