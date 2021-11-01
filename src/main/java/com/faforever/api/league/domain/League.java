package com.faforever.api.league.domain;

import com.faforever.api.data.domain.AbstractEntity;
import com.yahoo.elide.annotation.Include;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Setter
@Table(name = "league")
@Include(name = League.TYPE_NAME)
public class League extends AbstractEntity<League> {
  public static final String TYPE_NAME = "league";

  private String technicalName;
  private String imageKey;
  private String mediumImageKey;
  private String smallImageKey;
  private String nameKey;
  private String descriptionKey;

  @Column(name = "technical_name")
  public String getTechnicalName() {
    return technicalName;
  }

  @Column(name = "image_key")
  public String getImageKey() {
    return imageKey;
  }

  @Column(name = "medium_image_key")
  public String getMediumImageKey() {
    return mediumImageKey;
  }

  @Column(name = "small_image_key")
  public String getSmallImageKey() {
    return smallImageKey;
  }

  @Column(name = "name_key")
  public String getNameKey() {
    return nameKey;
  }

  @Column(name = "description_key")
  public String getDescriptionKey() {
    return descriptionKey;
  }
}
