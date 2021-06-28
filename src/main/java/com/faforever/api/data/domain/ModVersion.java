package com.faforever.api.data.domain;

import com.faforever.api.data.checks.Prefab;
import com.faforever.api.data.listeners.ModVersionEnricher;
import com.faforever.api.security.elide.permission.AdminModCheck;
import com.yahoo.elide.annotation.Audit;
import com.yahoo.elide.annotation.Audit.Action;
import com.yahoo.elide.annotation.ComputedAttribute;
import com.yahoo.elide.annotation.Exclude;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "mod_version")
@Include(name = ModVersion.TYPE_NAME)
@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@EntityListeners(ModVersionEnricher.class)
public class ModVersion implements DefaultEntity, OwnableEntity {

  public static final String TYPE_NAME = "modVersion";

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  @EqualsAndHashCode.Include
  private Integer id;

  @Column(name = "create_time")
  private OffsetDateTime createTime;

  @Column(name = "update_time")
  private OffsetDateTime updateTime;


  @Column(name = "uid")
  @EqualsAndHashCode.Include
  private String uid;

  @Column(name = "type")
  @Enumerated(EnumType.STRING)
  private ModType type;

  @Column(name = "description")
  private String description;

  @Column(name = "version")
  @EqualsAndHashCode.Include
  private short version;

  @Column(name = "filename")
  private String filename;

  @Column(name = "icon")
  // Excluded since I see no reason why this is even stored in the database.
  @Exclude
  private String icon;

  @UpdatePermission(expression = AdminModCheck.EXPRESSION)
  @Audit(action = Action.UPDATE, logStatement = "Updated mod version `{0}` attribute ranked to: {1}", logExpressions = {"${modVersion.id}", "${modVersion.ranked}"})
  @Column(name = "ranked")
  private boolean ranked;

  @UpdatePermission(expression = AdminModCheck.EXPRESSION)
  @Audit(action = Action.UPDATE, logStatement = "Updated mod version `{0}` attribute hidden to: {1}", logExpressions = {"${modVersion.id}", "${modVersion.hidden}"})
  @Column(name = "hidden")
  private boolean hidden;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "mod_id")
  private Mod mod;

  @Transient
  @ComputedAttribute
  private String thumbnailUrl;

  @Transient
  @ComputedAttribute
  private String downloadUrl;

  @OneToMany(mappedBy = "modVersion")
  @UpdatePermission(expression = Prefab.ALL)
  private List<ModVersionReview> reviews;

  @OneToOne(mappedBy = "modVersion")
  @UpdatePermission(expression = Prefab.ALL)
  private ModVersionReviewsSummary reviewsSummary;

  @Transient
  @Override
  public Login getEntityOwner() {
    return mod.getEntityOwner();
  }
}
