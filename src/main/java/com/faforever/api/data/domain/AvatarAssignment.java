package com.faforever.api.data.domain;

import com.faforever.api.data.checks.IsEntityOwner;
import com.faforever.api.security.elide.permission.WriteAvatarCheck;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import com.yahoo.elide.annotation.Audit;
import com.yahoo.elide.annotation.Audit.Action;
import com.yahoo.elide.annotation.CreatePermission;
import com.yahoo.elide.annotation.DeletePermission;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
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
@Table(name = "avatars")
@Include(name = AvatarAssignment.TYPE_NAME)
@CreatePermission(expression = WriteAvatarCheck.EXPRESSION)
@DeletePermission(expression = WriteAvatarCheck.EXPRESSION)
@Audit(action = Action.CREATE, logStatement = "Avatar ''{0}'' has been assigned to player ''{1}''", logExpressions = {"${avatarAssignment.avatar.id}", "${avatarAssignment.player.id}"})
@Audit(action = Action.DELETE, logStatement = "Avatar ''{0}'' has been revoked from player ''{1}''", logExpressions = {"${avatarAssignment.avatar.id}", "${avatarAssignment.player.id}"})
@Data
@NoArgsConstructor
@Type(AvatarAssignment.TYPE_NAME)
public class AvatarAssignment implements DefaultEntity, OwnableEntity {

  public static final String TYPE_NAME = "avatarAssignment";

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Integer id;

  @Column(name = "create_time")
  private OffsetDateTime createTime;

  @Column(name = "update_time")
  private OffsetDateTime updateTime;

  @Column(name = "selected")
  @UpdatePermission(expression = IsEntityOwner.EXPRESSION)
  @Audit(action = Action.UPDATE, logStatement = "Avatar ''{0}'' has been selected on player ''{1}''", logExpressions = {"${avatarAssignment.avatar.id}", "${avatarAssignment.player.id}"})
  private Boolean selected = Boolean.FALSE;

  @Column(name = "expires_at")
  @UpdatePermission(expression = WriteAvatarCheck.EXPRESSION)
  @Audit(action = Action.UPDATE, logStatement = "Expiration of avatar assignment ''{0}'' has been set to ''{1}''", logExpressions = {"${avatarAssignment.id}", "${avatarAssignment.expiresAt}"})
  private OffsetDateTime expiresAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "idUser")
  @NotNull
  @Relationship(Player.TYPE_NAME)
  private Player player;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "idAvatar")
  @NotNull
  @Relationship(Avatar.TYPE_NAME)
  private Avatar avatar;

  @Override
  @Transient
  public Login getEntityOwner() {
    return player;
  }
}
