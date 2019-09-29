package com.faforever.api.data.domain;

import com.faforever.api.data.checks.permission.IsModerator;
import com.yahoo.elide.annotation.Audit;
import com.yahoo.elide.annotation.Audit.Action;
import com.yahoo.elide.annotation.CreatePermission;
import com.yahoo.elide.annotation.DeletePermission;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.ReadPermission;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Getter
@Setter
@Table(name = "email_domain_blacklist")
@Include(type = "domainBlacklist", rootLevel = true)
@ReadPermission(expression = IsModerator.EXPRESSION)
@UpdatePermission(expression = IsModerator.EXPRESSION)
@CreatePermission(expression = IsModerator.EXPRESSION)
@DeletePermission(expression = IsModerator.EXPRESSION)
@Audit(action = Action.CREATE, logStatement = "Email domain `{0}` added to blacklist", logExpressions = "${domainBlacklist.domain}")
@Audit(action = Action.DELETE, logStatement = "Email domain `{0}` removed from blacklist", logExpressions = "${domainBlacklist.domain}")
@EqualsAndHashCode
public class DomainBlacklist {

  @Id
  @Column(name = "domain")
  private String domain;
}
