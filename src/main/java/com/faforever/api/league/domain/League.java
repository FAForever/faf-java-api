package com.faforever.api.league.domain;

import com.faforever.api.data.domain.DefaultEntity;
import com.yahoo.elide.annotation.Include;
import lombok.Data;
import lombok.EqualsAndHashCode;
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
@Table(name = "league")
@Include(name = League.TYPE_NAME)
public class League implements DefaultEntity {
  public static final String TYPE_NAME = "league";

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  @EqualsAndHashCode.Include
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
