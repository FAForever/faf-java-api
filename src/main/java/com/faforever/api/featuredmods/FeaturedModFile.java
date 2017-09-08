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
// Thanks to the dynamic table nature of the legacy updater, this class is not mapped to an underlying table but
// a native query instead. This is why the columns here can't be found in any table.
public class FeaturedModFile {

  private int id;
  private String group;
  private String md5;
  private String name;
  private short version;
  private String url;
  private String folderName;
  private int fileId;

  @Id
  @Column(name = "id")
  public int getId() {
    return id;
  }

  @Column(name = "\"group\"")
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

  @Column(name = "fileId")
  public int getFileId() {
    return fileId;
  }

  @Transient
  // Enriched by FeaturedModFileEnricher
  public String getUrl() {
    return url;
  }

  /**
   * Returns the name of the folder on the server in which the file resides (e.g. {@code updates_faf_files}). Used by
   * the {@link FeaturedModFileEnricher}.
   */
  @Column(name = "folderName")
  public String getFolderName() {
    return folderName;
  }
}
