package com.faforever.api.featuredmods;

import com.yahoo.elide.annotation.Exclude;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;

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
  private String originalFileName;
  private int version;
  // Enriched in FeaturedModFileEnricher
  private String url;
  private String cacheableUrl;
  private String hmacToken;
  private String hmacParameter;
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

  /**
   * Name of the file on the client's file system.
   */
  @Column(name = "name")
  public String getName() {
    return name;
  }


  /**
   * Name of the file on the server's file system.
   */
  @Column(name = "fileName")
  @Exclude
  public String getOriginalFileName() {
    return originalFileName;
  }

  @Column(name = "version")
  public int getVersion() {
    return version;
  }

  @Column(name = "fileId")
  public int getFileId() {
    return fileId;
  }

  /**
   * Returns the name of the folder on the server in which the file resides (e.g. {@code updates_faf_files}). Used by
   * the {@link FeaturedModFileEnricher}.
   */
  @Column(name = "folderName")
  public String getFolderName() {
    return folderName;
  }

  // Enriched by FeaturedModFileEnricher

  /**
   * URL with hmac token as query parameter for backwards compatibility with clients, cannot be cached by cloudflare
   */
  @Transient
  public String getUrl() {
    return url;
  }

  /**
   * URL without any query parameters so it can be cached by cloudflare
   */
  @Transient
  public String getCacheableUrl() {
    return cacheableUrl;
  }

  /**
   * Token to be set as header parameter for cloudflare validation
   */
  @Transient
  public String getHmacToken() {
    return hmacToken;
  }

  /**
   * Parameter to set the token as
   */
  @Transient
  public String getHmacParameter() {
    return hmacParameter;
  }
}
