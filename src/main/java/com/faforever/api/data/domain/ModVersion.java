package com.faforever.api.data.domain;

import com.faforever.api.data.checks.permission.IsModerator;
import com.faforever.api.data.listeners.ModVersionEnricher;
import com.yahoo.elide.annotation.Audit;
import com.yahoo.elide.annotation.Audit.Action;
import com.yahoo.elide.annotation.ComputedAttribute;
import com.yahoo.elide.annotation.Exclude;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.UpdatePermission;
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
@Setter
@EntityListeners(ModVersionEnricher.class)
public class ModVersion {

  public static final String TYPE_NAME = "modVersion";

  private Integer id;
  private String uid;
  private ModType type;
  private String description;
  private short version;
  private String filename;
  private String icon;
  private boolean ranked;
  private boolean hidden;
  private OffsetDateTime createTime;
  private OffsetDateTime updateTime;
  private Mod mod;
  private String thumbnailUrl;
  private String downloadUrl;
  private List<ModVersionReview> reviews;
  private ModVersionReviewsSummary reviewsSummary;

  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Integer getId() {
    return id;
  }

  @Column(name = "uid")
  public String getUid() {
    return uid;
  }

  @Column(name = "type")
  @Enumerated(EnumType.STRING)
  public ModType getType() {
    return type;
  }

  @Column(name = "description")
  public String getDescription() {
    return description;
  }

  @Column(name = "version")
  public short getVersion() {
    return version;
  }

  @Column(name = "filename")
  public String getFilename() {
    return filename;
  }

  @Column(name = "icon")
  // Excluded since I see no reason why this is even stored in the database.
  @Exclude
  public String getIcon() {
    return icon;
  }

  @UpdatePermission(expression = IsModerator.EXPRESSION)
  @Audit(action = Action.UPDATE, logStatement = "Updated mod version `{0}` attribute ranked to: {1}", logExpressions = {"${modVersion.id}", "${modVersion.ranked}"})
  @Column(name = "ranked")
  public boolean isRanked() {
    return ranked;
  }

  @UpdatePermission(expression = IsModerator.EXPRESSION)
  @Audit(action = Action.UPDATE, logStatement = "Updated mod version `{0}` attribute hidden to: {1}", logExpressions = {"${modVersion.id}", "${modVersion.hidden}"})
  @Column(name = "hidden")
  public boolean isHidden() {
    return hidden;
  }

  @Column(name = "create_time")
  public OffsetDateTime getCreateTime() {
    return createTime;
  }

  @Column(name = "update_time")
  public OffsetDateTime getUpdateTime() {
    return updateTime;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "mod_id")
  public Mod getMod() {
    return mod;
  }

  @Transient
  @ComputedAttribute
  public String getThumbnailUrl() {
    return thumbnailUrl;
  }

  @Transient
  @ComputedAttribute
  public String getDownloadUrl() {
    return downloadUrl;
  }

  @OneToMany(mappedBy = "modVersion")
  @UpdatePermission(expression = "Prefab.Role.All")
  public List<ModVersionReview> getReviews() {
    return reviews;
  }

  @OneToOne(mappedBy = "modVersion")
  @UpdatePermission(expression = "Prefab.Role.All")
  public ModVersionReviewsSummary getReviewsSummary() {
    return reviewsSummary;
  }
}
