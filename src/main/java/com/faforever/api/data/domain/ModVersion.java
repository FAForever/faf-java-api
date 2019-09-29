package com.faforever.api.data.domain;

import com.faforever.api.data.checks.permission.IsModerator;
import com.faforever.api.data.listeners.ModVersionEnricher;
import com.yahoo.elide.annotation.Audit;
import com.yahoo.elide.annotation.Audit.Action;
import com.yahoo.elide.annotation.ComputedAttribute;
import com.yahoo.elide.annotation.Exclude;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Getter;
import lombok.Setter;

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
@Include(rootLevel = true, type = ModVersion.TYPE_NAME)
@Getter
@Setter
@EntityListeners(ModVersionEnricher.class)
public class ModVersion {
  public static final String TYPE_NAME = "modVersion";

  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "uid")
  private String uid;

  @Column(name = "type")
  @Enumerated(EnumType.STRING)
  private ModType type;

  @Column(name = "description")
  private String description;

  @Column(name = "version")
  private short version;

  @Column(name = "filename")
  private String filename;

  @Column(name = "icon")
  // Excluded since I see no reason why this is even stored in the database.
  @Exclude
  private String icon;

  @UpdatePermission(expression = IsModerator.EXPRESSION)
  @Audit(action = Action.UPDATE, logStatement = "Updated mod version `{0}` attribute ranked to: {1}", logExpressions = {"${modVersion.id}", "${modVersion.ranked}"})
  @Column(name = "ranked")
  private boolean ranked;

  @UpdatePermission(expression = IsModerator.EXPRESSION)
  @Audit(action = Action.UPDATE, logStatement = "Updated mod version `{0}` attribute hidden to: {1}", logExpressions = {"${modVersion.id}", "${modVersion.hidden}"})
  @Column(name = "hidden")
  private boolean hidden;

  @Column(name = "create_time")
  private OffsetDateTime createTime;

  @Column(name = "update_time")
  private OffsetDateTime updateTime;

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
  @UpdatePermission(expression = "Prefab.Role.All")
  private List<ModVersionReview> reviews;

  @OneToOne(mappedBy = "modVersion")
  @UpdatePermission(expression = "Prefab.Role.All")
  private ModVersionReviewsSummary reviewsSummary;
}
