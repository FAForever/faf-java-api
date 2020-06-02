package com.faforever.api.data.domain;

import com.faforever.api.data.checks.Prefab;
import com.faforever.api.security.elide.permission.WriteAvatarCheck;
import com.yahoo.elide.annotation.ComputedAttribute;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Setter;
import org.springframework.data.annotation.Transient;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.util.List;

@Entity
@Table(name = "avatars_list")
@Include(rootLevel = true, type = com.faforever.api.dto.Avatar.TYPE)
@Setter
public class Avatar extends AbstractEntity {

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
