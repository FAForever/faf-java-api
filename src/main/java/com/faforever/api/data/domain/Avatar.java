package com.faforever.api.data.domain;

import com.faforever.api.data.checks.Prefab;
import com.faforever.api.data.listeners.AvatarEnricherListener;
import com.faforever.api.security.elide.permission.WriteAvatarCheck;
import com.github.jasminb.jsonapi.annotations.Type;
import com.yahoo.elide.annotation.ComputedAttribute;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Setter;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Store;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import java.util.List;

@Entity
@Table(name = "avatars_list")
@Include(rootLevel = true, type = Avatar.TYPE_NAME)
@Setter
@Type(Avatar.TYPE_NAME)
@EntityListeners(AvatarEnricherListener.class)
public class Avatar extends AbstractEntity {

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
  @Field(index = Index.YES, analyze = Analyze.YES,
    store = Store.NO)
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
