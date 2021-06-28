package com.faforever.api.data.domain;

import com.faforever.api.data.checks.Prefab;
import com.faforever.api.data.listeners.AvatarEnricherListener;
import com.faforever.api.security.elide.permission.WriteAvatarCheck;
import com.github.jasminb.jsonapi.annotations.Type;
import com.yahoo.elide.annotation.ComputedAttribute;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "avatars_list")
@Include(name = Avatar.TYPE_NAME)
@Data
@NoArgsConstructor
@Type(Avatar.TYPE_NAME)
@EntityListeners(AvatarEnricherListener.class)
public class Avatar implements DefaultEntity {

  public static final String TYPE_NAME = "avatar";

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Integer id;

  @Column(name = "create_time")
  private OffsetDateTime createTime;

  @Column(name = "update_time")
  private OffsetDateTime updateTime;

  @Transient
  @ComputedAttribute
  private String url;

  @Column(name = "tooltip")
  @UpdatePermission(expression = WriteAvatarCheck.EXPRESSION)
  private String tooltip;

  @Column(name = "filename")
  @NotNull
  private String filename;

  // Cascading is needed for Create & Delete
  @OneToMany(mappedBy = "avatar", cascade = CascadeType.ALL, orphanRemoval = true)
  // Permission is managed by AvatarAssignment class
  @UpdatePermission(expression = Prefab.ALL)
  @EqualsAndHashCode.Exclude
  private List<AvatarAssignment> assignments;
}
