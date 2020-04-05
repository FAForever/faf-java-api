package com.faforever.api.data.domain;

import com.faforever.api.data.checks.IsEntityOwner;
import com.faforever.api.data.checks.Prefab;
import com.faforever.api.security.elide.permission.WriteAvatarCheck;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import com.yahoo.elide.annotation.Audit;
import com.yahoo.elide.annotation.Audit.Action;
import com.yahoo.elide.annotation.CreatePermission;
import com.yahoo.elide.annotation.DeletePermission;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;

@Entity
@Table(name = "avatars")
@Include(rootLevel = true, type = AvatarAssignment.TYPE_NAME)
@CreatePermission(expression = WriteAvatarCheck.EXPRESSION)
@DeletePermission(expression = WriteAvatarCheck.EXPRESSION)
@Audit(action = Action.CREATE, logStatement = "Avatar ''{0}'' has been assigned to player ''{1}''", logExpressions = {"${avatarAssignment.avatar.id}", "${avatarAssignment.player.id}"})
@Audit(action = Action.DELETE, logStatement = "Avatar ''{0}'' has been revoked from player ''{1}''", logExpressions = {"${avatarAssignment.avatar.id}", "${avatarAssignment.player.id}"})
@Setter
@Type(AvatarAssignment.TYPE_NAME)
public class AvatarAssignment extends AbstractEntity implements OwnableEntity {
  public static final String TYPE_NAME = "avatarAssignment";

  private Boolean selected = Boolean.FALSE;
  private OffsetDateTime expiresAt;
  @Relationship(Player.TYPE_NAME)
  private Player player;
  @Relationship(Avatar.TYPE_NAME)
  private Avatar avatar;

  @Column(name = "selected")
  @UpdatePermission(expression = IsEntityOwner.EXPRESSION)
  @Audit(action = Action.UPDATE, logStatement = "Avatar ''{0}'' has been selected on player ''{1}''", logExpressions = {"${avatarAssignment.avatar.id}", "${avatarAssignment.player.id}"})
  public Boolean isSelected() {
    return selected;
  }

  @Column(name = "expires_at")
  @UpdatePermission(expression = WriteAvatarCheck.EXPRESSION + " or " + Prefab.UPDATE_ON_CREATE)
  @Audit(action = Action.UPDATE, logStatement = "Expiration of avatar assignment ''{0}'' has been set to ''{1}''", logExpressions = {"${avatarAssignment.id}", "${avatarAssignment.expiresAt}"})
  public OffsetDateTime getExpiresAt() {
    return expiresAt;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "idAvatar")
  @NotNull
  @UpdatePermission(expression = Prefab.UPDATE_ON_CREATE)
  public Avatar getAvatar() {
    return avatar;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "idUser")
  @NotNull
  @UpdatePermission(expression = Prefab.UPDATE_ON_CREATE)
  public Player getPlayer() {
    return player;
  }

  @Override
  @Transient
  public Login getEntityOwner() {
    return getPlayer();
  }
}
