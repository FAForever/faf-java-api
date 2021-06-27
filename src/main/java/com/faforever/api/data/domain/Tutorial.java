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
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;

@Entity
@Table(name = "tutorial")
@Data
@NoArgsConstructor
@Include(name = Tutorial.TYPE_NAME)
@DeletePermission(expression = WriteTutorialCheck.EXPRESSION)
@UpdatePermission(expression = WriteTutorialCheck.EXPRESSION)
@CreatePermission(expression = WriteTutorialCheck.EXPRESSION)
@Audit(action = Action.DELETE, logStatement = "Tutorial with name `{0}` and ID `{1}` deleted", logExpressions = {"${tutorial.title}", "${tutorial.id}"})
@Audit(action = Action.CREATE, logStatement = "Tutorial with name `{0}` and ID `{1}` created", logExpressions = {"${tutorial.title}", "${tutorial.id}"})
@ReadPermission(expression = Prefab.ALL)
@EntityListeners(TutorialEnricherListener.class)
@Type(Tutorial.TYPE_NAME)
public class Tutorial implements DefaultEntity {

  public static final String TYPE_NAME = "tutorial";

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Integer id;

  @Column(name = "create_time")
  private OffsetDateTime createTime;

  @Column(name = "update_time")
  private OffsetDateTime updateTime;

  @Column(name = "description_key")
  private String descriptionKey;

  @Transient
  @ComputedAttribute
  private String description;

  @Column(name = "title_key")
  @NotNull
  private String titleKey;

  @Transient
  @ComputedAttribute
  private String title;

  @ManyToOne
  @JoinColumn(name = "category")
  @Nullable
  private TutorialCategory category;

  @Column(name = "image")
  @Nullable
  private String image;

  @ComputedAttribute
  @Transient
  @Nullable
  private String imageUrl;

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

  @Column(name = "technical_name")
  @NotNull
  private String technicalName;
}
