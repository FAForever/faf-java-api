package com.faforever.api.data.domain;

import com.faforever.api.data.checks.permission.IsModerator;
import com.yahoo.elide.annotation.Audit;
import com.yahoo.elide.annotation.Audit.Action;
import com.yahoo.elide.annotation.CreatePermission;
import com.yahoo.elide.annotation.DeletePermission;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.ReadPermission;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "user_notes")
@Include(rootLevel = true, type = "userNote")
@ReadPermission(expression = IsModerator.EXPRESSION)
@CreatePermission(expression = IsModerator.EXPRESSION)
@UpdatePermission(expression = IsModerator.EXPRESSION)
@DeletePermission(expression = "Prefab.Role.None")
@Audit(action = Action.CREATE, logStatement = "Note `{0}` for user `{1}` added (watched=`{2}`) with text: {3}", logExpressions = {"${userNote.id}", "${userNote.player.id}", "${userNote.watched}", "${userNote.note}"})
@Getter
@Setter
public class UserNote extends AbstractEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  @NotNull
  private Player player;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "author")
  @NotNull
  private Player author;

  @Audit(action = Action.UPDATE, logStatement = "Note `{0}` for user `{1}` update with watched: {2}`", logExpressions = {"${userNote.id}", "${userNote.player.id}", "${userNote.watched}"})
  @Column(name = "watched")
  private boolean watched;

  @Audit(action = Action.UPDATE, logStatement = "Note `{0}` for user `{1}` updated with text: {2}", logExpressions = {"${userNote.id}", "${userNote.player.id}", "${userNote.note}"})
  @Column(name = "note")
  @NotNull
  private String note;
}
