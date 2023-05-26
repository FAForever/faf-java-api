package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "license")
@Include(name = License.TYPE_NAME)
@Setter
public class License {

  public static final String TYPE_NAME = "license";

  private int id;

  private String name;
  private String shortName;
  private String url;
  private String licenseText;
  private boolean active;
  private boolean revocable;
  private boolean redistributable;
  private boolean modifiable;

  @Id
  @Column(name = "id")
  public int getId() {
    return id;
  }

  @Column(name = "name")
  public String getName() {
    return name;
  }

  @Column(name = "short_name")
  public String getShortName() {
    return shortName;
  }

  @Column(name = "url")
  public String getUrl() {
    return url;
  }

  @Column(name = "license_text")
  public String getLicenseText() {
    return licenseText;
  }

  @Column(name = "active")
  public boolean isActive() {
    return active;
  }

  @Column(name = "redistributable")
  public boolean isRedistributable() {
    return redistributable;
  }

  @Column(name = "revocable")
  public boolean isRevocable() {
    return revocable;
  }

  @Column(name = "modifiable")
  public boolean isModifiable() {
    return modifiable;
  }

  public boolean isLessPermissiveThan(License other) {
    return (!redistributable && other.redistributable)
      || (!modifiable && other.modifiable);
  }
}
