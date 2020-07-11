package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Setter
@Table(name = "leaderboard")
@Include(rootLevel = true, type = Leaderboard.TYPE_NAME)
public class Leaderboard extends AbstractEntity {

  public static final String TYPE_NAME = "leaderboard";

  private String technical_name;
  private String name_key;
  private String description_key;

  @Column(name = "technical_name")
  public String getTechnical_name() {
    return technical_name;
  }

  @Column(name = "name_key")
  public String getName_key() {
    return name_key;
  }

  @Column(name = "description_key")
  public String getDescription_key() {
    return description_key;
  }
}
