package com.faforever.api.data.domain;

import com.faforever.api.data.checks.Prefab;
import com.faforever.api.data.listeners.TutorialEnricherListener;
import com.faforever.api.security.elide.permission.WriteTutorialCheck;
import com.github.jasminb.jsonapi.annotations.Type;
import com.yahoo.elide.annotation.Audit;
import com.yahoo.elide.annotation.Audit.Action;
import com.yahoo.elide.annotation.ComputedAttribute;
import com.yahoo.elide.annotation.CreatePermission;
import com.yahoo.elide.annotation.DeletePermission;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.ReadPermission;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "tutorial")
@Setter
@Include(rootLevel = true, type = Tutorial.TYPE_NAME)
@DeletePermission(expression = WriteTutorialCheck.EXPRESSION)
@UpdatePermission(expression = WriteTutorialCheck.EXPRESSION)
@CreatePermission(expression = WriteTutorialCheck.EXPRESSION)
@Audit(action = Action.DELETE, logStatement = "Tutorial with name `{0}` and ID `{1}` deleted", logExpressions = {"${tutorial.title}", "${tutorial.id}"})
@Audit(action = Action.CREATE, logStatement = "Tutorial with name `{0}` and ID `{1}` created", logExpressions = {"${tutorial.title}", "${tutorial.id}"})
@ReadPermission(expression = Prefab.ALL)
@EntityListeners(TutorialEnricherListener.class)
@Type(Tutorial.TYPE_NAME)
public class Tutorial extends AbstractEntity {
  public static final String TYPE_NAME = "tutorial";
  private String descriptionKey;
  private String description;
  private String titleKey;
  private String title;
  private TutorialCategory category;
  private String image;
  private String imageUrl;
  private Integer ordinal;
  private Boolean launchable;
  private MapVersion mapVersion;
  private String technicalName;

  @Transient
  @ComputedAttribute
  public String getDescription() {
    return description;
  }

  @Transient
  @ComputedAttribute
  public String getTitle() {
    return title;
  }

  @ManyToOne()
  @JoinColumn(name = "category")
  @Nullable
  public TutorialCategory getCategory() {
    return category;
  }

  @NotNull
  @Column(name = "ordinal")
  public Integer getOrdinal() {
    return ordinal;
  }

  @NotNull
  @Column(name = "launchable")
  public Boolean getLaunchable() {
    return launchable;
  }

  @ManyToOne
  @Nullable
  @JoinColumn(name = "map_version_id")
  public MapVersion getMapVersion() {
    return mapVersion;
  }

  @Column(name = "description_key")
  public String getDescriptionKey() {
    return descriptionKey;
  }

  @Column(name = "title_key")
  @NotNull
  public String getTitleKey() {
    return titleKey;
  }

  @Column(name = "image")
  @Nullable
  public String getImage() {
    return image;
  }

  @ComputedAttribute
  @Transient
  @Nullable
  public String getImageUrl() {
    return imageUrl;
  }

  @Column(name = "technical_name")
  @NotNull
  public String getTechnicalName() {
    return technicalName;
  }
}
