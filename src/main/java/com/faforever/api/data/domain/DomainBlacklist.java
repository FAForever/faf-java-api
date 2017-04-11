package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import lombok.EqualsAndHashCode;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Setter
@Table(name = "email_domain_blacklist")
@Include(type = "domainBlacklist")
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
