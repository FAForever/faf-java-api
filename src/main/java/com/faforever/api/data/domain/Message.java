package com.faforever.api.data.domain;

import com.faforever.api.data.checks.Prefab;
import com.faforever.api.data.listeners.MessageReloadListener;
import com.faforever.api.security.elide.permission.WriteMessageCheck;
import com.github.jasminb.jsonapi.annotations.Type;
import com.yahoo.elide.annotation.Audit;
import com.yahoo.elide.annotation.Audit.Action;
import com.yahoo.elide.annotation.CreatePermission;
import com.yahoo.elide.annotation.DeletePermission;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.ReadPermission;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "messages")
@Data
@NoArgsConstructor
@Include
@DeletePermission(expression = WriteMessageCheck.EXPRESSION)
@UpdatePermission(expression = WriteMessageCheck.EXPRESSION)
@CreatePermission(expression = WriteMessageCheck.EXPRESSION)
@Audit(action = Action.DELETE, logStatement = "Message with key `{0}` , ID `{1}`, language `{2}` , region `{3}` and value `{4}` deleted", logExpressions = {"${message.key}", "${message.id}", "${message.language}", "${message.region}", "${message.value}"})
@Audit(action = Action.CREATE, logStatement = "Message with key `{0}` and ID `{1}` created", logExpressions = {"${message.key}", "${message.id}"})
@ReadPermission(expression = Prefab.ALL)
@EntityListeners(MessageReloadListener.class)
@Type(Message.TYPE_NAME)
public class Message {

  public static final String TYPE_NAME = "message";

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Integer id;

  @Column(name = "`key`")
  private String key;

  @Column(name = "language")
  private String language;

  @Column(name = "region")
  private String region;

  @Column(name = "value")
  private String value;
}
