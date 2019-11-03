package com.faforever.api.data.domain;

import com.faforever.api.data.checks.Prefab;
import com.faforever.api.security.elide.permission.AdminAccountNoteCheck;
import com.yahoo.elide.annotation.Audit;
import com.yahoo.elide.annotation.Audit.Action;
import com.yahoo.elide.annotation.CreatePermission;
import com.yahoo.elide.annotation.DeletePermission;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.ReadPermission;
import com.yahoo.elide.annotation.UpdatePermission;
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
@ReadPermission(expression = AdminAccountNoteCheck.EXPRESSION)
@CreatePermission(expression = AdminAccountNoteCheck.EXPRESSION)
@UpdatePermission(expression = AdminAccountNoteCheck.EXPRESSION)
@DeletePermission(expression = Prefab.NONE)
@Audit(action = Action.CREATE, logStatement = "Note `{0}` for user `{1}` added (watched=`{2}`) with text: {3}", logExpressions = {"${userNote.id}", "${userNote.player.id}", "${userNote.watched}", "${userNote.note}"})
@Setter
public class UserNote extends AbstractEntity {
  private Player player;
  private Player author;
  private boolean watched;
  private String note;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  @NotNull
  public Player getPlayer() {
    return player;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "author")
  @NotNull
  public Player getAuthor() {
    return author;
  }

  @Audit(action = Action.UPDATE, logStatement = "Note `{0}` for user `{1}` update with watched: {2}`", logExpressions = {"${userNote.id}", "${userNote.player.id}", "${userNote.watched}"})
  @Column(name = "watched")
  public boolean isWatched() {
    return watched;
  }

  @Audit(action = Action.UPDATE, logStatement = "Note `{0}` for user `{1}` updated with text: {2}", logExpressions = {"${userNote.id}", "${userNote.player.id}", "${userNote.note}"})
  @Column(name = "note")
  @NotNull
  public String getNote() {
    return note;
  }
}
