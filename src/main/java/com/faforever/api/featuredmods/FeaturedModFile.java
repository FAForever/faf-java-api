package com.faforever.api.featuredmods;

import com.yahoo.elide.annotation.Exclude;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.Transient;

@Getter
@Setter
@Immutable
@Entity
@EntityListeners(FeaturedModFileEnricher.class)
// Thanks to the dynamic table nature of the legacy updater, this class is not mapped to an underlying table but
// a native query instead. This is why the columns here can't be found in any table.
public class FeaturedModFile {

  @Id
  @Column(name = "id")
  private int id;

  @Column(name = "\"group\"")
  private String group;

  @Column(name = "md5")
  private String md5;

  // Name of the file on the client's file system.
  @Column(name = "name")
  private String name;

  // Name of the file on the server's file system.
  @Column(name = "fileName")
  @Exclude
  private String originalFileName;

  @Column(name = "version")
  private int version;

  @Column(name = "fileId")
  private short fileId;

  @Transient
  // Enriched by FeaturedModFileEnricher
  private String url;

  // Name of the folder on the server in which the file resides (e.g. {@code updates_faf_files}).
  // Used by the FeaturedModFileEnricher
  @Column(name = "folderName")
  private String folderName;
}
