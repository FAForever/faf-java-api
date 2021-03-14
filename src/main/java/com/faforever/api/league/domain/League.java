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
@Include(type = League.TYPE_NAME)
public class League extends AbstractEntity {
  public static final String TYPE_NAME = "leagueV2";

  private String technicalName;
  private String nameKey;
  private String descriptionKey;

  @Column(name = "technical_name")
  public String getTechnicalName() {
    return technicalName;
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
