package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "leaderboard")
@Include(name = Leaderboard.TYPE_NAME)
public class Leaderboard implements DefaultEntity {

  public static final String TYPE_NAME = "leaderboard";

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Integer id;

  @Column(name = "create_time")
  private OffsetDateTime createTime;

  @Column(name = "update_time")
  private OffsetDateTime updateTime;

  @Column(name = "technical_name")
  private String technicalName;

  @Column(name = "name_key")
  private String nameKey;

  @Column(name = "description_key")
  private String descriptionKey;
}
