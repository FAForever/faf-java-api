package com.faforever.api.data.domain;

import com.faforever.api.data.checks.permission.IsModerator;
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
@ReadPermission(expression = IsModerator.EXPRESSION)
@UpdatePermission(expression = IsModerator.EXPRESSION)
@CreatePermission(expression = IsModerator.EXPRESSION)
@DeletePermission(expression = IsModerator.EXPRESSION)
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
