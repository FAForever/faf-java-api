package com.faforever.api.data.domain;

import com.faforever.api.data.converter.JsonConverter;
import com.faforever.api.security.elide.permission.WriteMatchmakerMapCheck;
import com.yahoo.elide.annotation.CreatePermission;
import com.yahoo.elide.annotation.DeletePermission;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.time.OffsetDateTime;

import static com.faforever.api.data.domain.MapPoolAssignment.TYPE_NAME;


@Entity
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@Table(name = "map_pool_map_version")
@Include(name = TYPE_NAME)
@CreatePermission(expression = WriteMatchmakerMapCheck.EXPRESSION)
@UpdatePermission(expression = WriteMatchmakerMapCheck.EXPRESSION)
@DeletePermission(expression = WriteMatchmakerMapCheck.EXPRESSION)
public class MapPoolAssignment implements DefaultEntity {

  public static final String TYPE_NAME = "mapPoolAssignment";

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  @EqualsAndHashCode.Include
  private Integer id;

  @Column(name = "create_time")
  private OffsetDateTime createTime;

  @Column(name = "update_time")
  private OffsetDateTime updateTime;

  @OneToOne
  @JoinColumn(name = "map_pool_id")
  @NotNull
  private MapPool mapPool;

  @OneToOne
  @JoinColumn(name = "map_version_id")
  private MapVersion mapVersion;

  @Column(name = "weight")
  @NotNull
  private Integer weight;

  @Column(name = "map_params")
  @Convert(converter = JsonConverter.class)
  private java.util.Map<String, Object> mapParams;
}
