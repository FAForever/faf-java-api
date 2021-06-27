package com.faforever.api.data.domain;


import com.faforever.api.data.checks.UserGroupPublicCheck;
import com.faforever.api.security.elide.permission.WriteUserGroupCheck;
import com.yahoo.elide.annotation.CreatePermission;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.ReadPermission;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.Set;

@Entity
@Table(name = "user_group")
@Include(name = "userGroup")
@UpdatePermission(expression = WriteUserGroupCheck.EXPRESSION)
@CreatePermission(expression = WriteUserGroupCheck.EXPRESSION)
@ReadPermission(expression = UserGroupPublicCheck.EXPRESSION + " or " + WriteUserGroupCheck.EXPRESSION)
@Data
@NoArgsConstructor
public class UserGroup implements DefaultEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Integer id;

  @Column(name = "create_time")
  private OffsetDateTime createTime;

  @Column(name = "update_time")
  private OffsetDateTime updateTime;

  @Column(name = "technical_name")
  private String technicalName;

  @Column(name = "name_key")
  private String nameKey;

  @Column(name = "public")
  private boolean public_;

  @ManyToOne
  @JoinColumn(name = "parent_group_id")
  private UserGroup parent;

  @OneToMany(mappedBy = "parent")
  private Set<UserGroup> children;
  @JoinTable(name = "user_group_assignment",
    joinColumns = @JoinColumn(name = "group_id"),
    inverseJoinColumns = @JoinColumn(name = "user_id")
  )
  @ManyToMany(cascade = {
    CascadeType.PERSIST,
    CascadeType.MERGE
  })
  @NotNull
  @Valid
  private Set<Player> members;


  @JoinTable(name = "group_permission_assignment",
    joinColumns = @JoinColumn(name = "group_id"),
    inverseJoinColumns = @JoinColumn(name = "permission_id")
  )
  @ManyToMany(cascade = {
    CascadeType.PERSIST,
    CascadeType.MERGE
  })
  @NotNull
  @Valid
  private Set<GroupPermission> permissions;
}
