package com.faforever.api.data.domain;

import com.faforever.api.security.elide.permission.WriteEmailDomainBanCheck;
import com.yahoo.elide.annotation.Audit;
import com.yahoo.elide.annotation.Audit.Action;
import com.yahoo.elide.annotation.CreatePermission;
import com.yahoo.elide.annotation.DeletePermission;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.ReadPermission;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.EqualsAndHashCode;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Setter
@Table(name = "email_domain_blacklist")
@Include(type = "domainBlacklist", rootLevel = true)
@ReadPermission(expression = WriteEmailDomainBanCheck.EXPRESSION)
@UpdatePermission(expression = WriteEmailDomainBanCheck.EXPRESSION)
@CreatePermission(expression = WriteEmailDomainBanCheck.EXPRESSION)
@DeletePermission(expression = WriteEmailDomainBanCheck.EXPRESSION)
@Audit(action = Action.CREATE, logStatement = "Email domain `{0}` added to blacklist", logExpressions = "${domainBlacklist.domain}")
@Audit(action = Action.DELETE, logStatement = "Email domain `{0}` removed from blacklist", logExpressions = "${domainBlacklist.domain}")
@EqualsAndHashCode
public class DomainBlacklist {
  private String domain;

  @Id
  @Column(name = "domain")
  public String getDomain() {
    return domain;
  }

  public void setDomain(String domain) {
    this.domain = domain;
  }
}
