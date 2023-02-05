package com.faforever.api.league.domain;

import com.faforever.api.data.domain.AbstractEntity;
import com.yahoo.elide.annotation.Exclude;
import com.yahoo.elide.annotation.Include;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Setter
@Table(name = "league")
@Include(name = League.TYPE_NAME)
public class League extends AbstractEntity<League> {
  public static final String TYPE_NAME = "league";

  private String technicalName;
  private boolean enabled;
  private String nameKey;
  private String descriptionKey;
  private String imageUrl;
  private String mediumImageUrl;
  private String smallImageUrl;

  @Column(name = "technical_name")
  public String getTechnicalName() {
    return technicalName;
  }

  @Column(name = "enabled")
  public boolean isEnabled() {
    return enabled;
  }

  @Column(name = "name_key")
  public String getNameKey() {
    return nameKey;
  }

  @Column(name = "description_key")
  public String getDescriptionKey() {
    return descriptionKey;
  }

  @Exclude
  @Column(name = "image_url")
  public String getImageUrl() {
    return imageUrl;
  }

  @Exclude
  @Column(name = "medium_image_url")
  public String getMediumImageUrl() {
    return mediumImageUrl;
  }

  @Exclude
  @Column(name = "small_image_url")
  public String getSmallImageUrl() {
    return smallImageUrl;
  }
}
