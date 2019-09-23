package com.faforever.api.data.domain;


import com.faforever.api.data.checks.Prefab;
import com.faforever.api.data.listeners.TutorialCategoryEnricherListener;
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
import java.util.List;

@Entity
@Table(name = "tutorial_category")
@Setter
@Include(rootLevel = true, type = TutorialCategory.TYPE_NAME)
@DeletePermission(expression = WriteTutorialCheck.EXPRESSION)
@UpdatePermission(expression = WriteTutorialCheck.EXPRESSION)
@CreatePermission(expression = WriteTutorialCheck.EXPRESSION)
@Audit(action = Action.DELETE, logStatement = "Tutorial Category with title `{0}` and ID `{1}` deleted", logExpressions = {"${tutorialCategory.category}", "${tutorialCategory.id}"})
@Audit(action = Action.CREATE, logStatement = "Tutorial Category with title `{0}` and ID `{1}` created", logExpressions = {"${tutorialCategory.category}", "${tutorialCategory.id}"})
@ReadPermission(expression = Prefab.ALL)
@EntityListeners(TutorialCategoryEnricherListener.class)
@Type(TutorialCategory.TYPE_NAME)
public class TutorialCategory {
  public static final String TYPE_NAME = "tutorialCategory";

  private int id;
  private String categoryKey;
  private String category;
  private List<Tutorial> tutorials;

  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Id
  @Column(name = "id")
  public int getId() {
    return id;
  }

  @Column(name = "category_key")
  @NotNull
  public String getCategoryKey() {
    return categoryKey;
  }

  @Transient
  @ComputedAttribute
  public String getCategory() {
    return category;
  }

  @OneToMany(mappedBy = "category", cascade = CascadeType.REMOVE)
  public List<Tutorial> getTutorials() {
    return tutorials;
  }
}
