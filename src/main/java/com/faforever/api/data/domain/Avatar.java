package com.faforever.api.data.domain;

import com.faforever.api.data.checks.permission.IsModerator;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.util.List;

@Entity
@Table(name = "avatars_list")
@Include(rootLevel = true, type = Avatar.TYPE_NAME)
@Setter
public class Avatar extends AbstractEntity {

  public static final String TYPE_NAME = "avatar";

  private String url;
  private String tooltip;
  private List<AvatarAssignment> assignments;

  @Column(name = "url")
  @NotNull
  public String getUrl() {
    return url;
  }

  @Column(name = "tooltip")
  @UpdatePermission(expression = IsModerator.EXPRESSION)
  public String getTooltip() {
    return tooltip;
  }

  // Cascading is needed for Create & Delete
  @OneToMany(mappedBy = "avatar", cascade = CascadeType.ALL, orphanRemoval = true)
  // Permission is managed by AvatarAssignment class
  @UpdatePermission(expression = "Prefab.Role.All")
  public List<AvatarAssignment> getAssignments() {
    return this.assignments;
  }

}
