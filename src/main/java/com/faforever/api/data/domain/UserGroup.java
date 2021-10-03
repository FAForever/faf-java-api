package com.faforever.api.data.domain;


import com.faforever.api.data.checks.UserGroupPublicCheck;
import com.faforever.api.security.elide.permission.ReadUserGroupCheck;
import com.faforever.api.security.elide.permission.WriteUserGroupCheck;
import com.yahoo.elide.annotation.CreatePermission;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.ReadPermission;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Entity
@Table(name = "user_group")
@Include(name = "userGroup")
@UpdatePermission(expression = WriteUserGroupCheck.EXPRESSION)
@CreatePermission(expression = WriteUserGroupCheck.EXPRESSION)
@ReadPermission(expression = UserGroupPublicCheck.EXPRESSION + " or " + ReadUserGroupCheck.EXPRESSION)
@Setter
public class
UserGroup extends AbstractEntity<UserGroup> {

  private String technicalName;
  private String nameKey;
  private boolean public_;
  private UserGroup parent;
  private Set<UserGroup> children;
  private Set<Player> members;
  private Set<GroupPermission> permissions;

  @Column(name = "technical_name")
  public String getTechnicalName() {
    return technicalName;
  }

  @Column(name = "name_key")
  public String getNameKey() {
    return nameKey;
  }

  @Column(name = "public")
  public boolean isPublic() {
    return public_;
  }

  @ManyToOne
  @JoinColumn(name = "parent_group_id")
  public UserGroup getParent() {
    return parent;
  }

  @OneToMany(mappedBy = "parent")
  public Set<UserGroup> getChildren() {
    return children;
  }

  public void setPublic(boolean public_) {
    this.public_ = public_;
  }

  @JoinTable(name = "user_group_assignment",
    joinColumns = @JoinColumn(name = "group_id"),
    inverseJoinColumns = @JoinColumn(name = "user_id")
  )
  @ManyToMany(cascade = {
    CascadeType.PERSIST,
    CascadeType.MERGE
  })
  public @NotNull
  @Valid Set<Player> getMembers() {
    return members;
  }

  @JoinTable(name = "group_permission_assignment",
    joinColumns = @JoinColumn(name = "group_id"),
    inverseJoinColumns = @JoinColumn(name = "permission_id")
  )
  @ManyToMany(cascade = {
    CascadeType.PERSIST,
    CascadeType.MERGE
  })
  public @NotNull
  @Valid Set<GroupPermission> getPermissions() {
    return permissions;
  }
}
