package com.faforever.api.data.domain;

import com.faforever.api.data.checks.Prefab;
import com.faforever.api.data.listeners.AvatarEnricherListener;
import com.faforever.api.security.elide.permission.WriteAvatarCheck;
import com.github.jasminb.jsonapi.annotations.Type;
import com.yahoo.elide.annotation.ComputedAttribute;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Setter;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Entity
@Table(name = "avatars_list")
@Include(name = Avatar.TYPE_NAME)
@Setter
@Type(Avatar.TYPE_NAME)
@EntityListeners(AvatarEnricherListener.class)
public class Avatar extends AbstractEntity<Avatar> {

  public static final String TYPE_NAME = "avatar";

  private String url;
  private String tooltip;
  private String filename;
  private List<AvatarAssignment> assignments;

  @Transient
  @ComputedAttribute
  public String getUrl() {
    return url;
  }

  @Column(name = "filename")
  @NotNull
  public String getFilename() { return filename; }

  @Column(name = "tooltip")
  @UpdatePermission(expression = WriteAvatarCheck.EXPRESSION)
  public String getTooltip() {
    return tooltip;
  }

  // Cascading is needed for Create & Delete
  @OneToMany(mappedBy = "avatar", cascade = CascadeType.ALL, orphanRemoval = true)
  // Permission is managed by AvatarAssignment class
  @UpdatePermission(expression = Prefab.ALL)
  public List<AvatarAssignment> getAssignments() {
    return this.assignments;
  }

}
