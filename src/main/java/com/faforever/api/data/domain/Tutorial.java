package com.faforever.api.data.domain;

import com.faforever.api.data.checks.permission.IsModerator;
import com.faforever.api.data.listeners.TutorialEnricherListener;
import com.github.jasminb.jsonapi.annotations.Type;
import com.yahoo.elide.annotation.Audit;
import com.yahoo.elide.annotation.Audit.Action;
import com.yahoo.elide.annotation.ComputedAttribute;
import com.yahoo.elide.annotation.CreatePermission;
import com.yahoo.elide.annotation.DeletePermission;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.ReadPermission;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Getter;
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
@Getter
@Setter
@Include(rootLevel = true, type = Tutorial.TYPE_NAME)
@DeletePermission(expression = IsModerator.EXPRESSION)
@UpdatePermission(expression = IsModerator.EXPRESSION)
@CreatePermission(expression = IsModerator.EXPRESSION)
@Audit(action = Action.DELETE, logStatement = "Tutorial with name `{0}` and ID `{1}` deleted", logExpressions = {"${tutorial.title}", "${tutorial.id}"})
@Audit(action = Action.CREATE, logStatement = "Tutorial with name `{0}` and ID `{1}` created", logExpressions = {"${tutorial.title}", "${tutorial.id}"})
@ReadPermission(expression = "Prefab.Role.All")
@EntityListeners(TutorialEnricherListener.class)
@Type(Tutorial.TYPE_NAME)
public class Tutorial extends AbstractEntity {
  public static final String TYPE_NAME = "tutorial";

  @Transient
  @ComputedAttribute
  private String description;

  @Transient
  @ComputedAttribute
  private String title;

  @ManyToOne()
  @JoinColumn(name = "category")
  @Nullable
  private TutorialCategory category;

  @NotNull
  @Column(name = "ordinal")
  private Integer ordinal;

  @NotNull
  @Column(name = "launchable")
  private Boolean launchable;

  @ManyToOne
  @Nullable
  @JoinColumn(name = "map_version_id")
  private MapVersion mapVersion;

  @Column(name = "description_key")
  private String descriptionKey;

  @Column(name = "title_key")
  @NotNull
  private String titleKey;

  @Column(name = "image")
  @Nullable
  private String image;

  @ComputedAttribute
  @Transient
  @Nullable
  private String imageUrl;

  @Column(name = "technical_name")
  @NotNull
  private String technicalName;
}
