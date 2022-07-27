package com.faforever.api.data.domain;

import com.faforever.api.data.checks.IsEntityOwner;
import com.faforever.api.data.checks.Prefab;
import com.faforever.api.security.elide.permission.ReadAccountPrivateDetailsCheck;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.yahoo.elide.annotation.Exclude;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.ReadPermission;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "service_links")
@Include(name = AccountLink.TYPE_NAME, rootLevel = false)
@Setter
@ReadPermission(expression = IsEntityOwner.EXPRESSION + " OR " + ReadAccountPrivateDetailsCheck.EXPRESSION)
public class AccountLink implements OwnableEntity {

  public static final String TYPE_NAME = "accountLink";
  private String id;
  private User user;
  private LinkedServiceType serviceType;
  private String serviceId;
  private boolean public_;
  private boolean ownership;

  @Id
  @Column(name = "id")
  public String getId() {
    return id;
  }

  @Column(name = "type")
  @Enumerated(EnumType.STRING)
  public LinkedServiceType getServiceType() {
    return serviceType;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  @Exclude
  public User getUser() {
    return user;
  }

  @Column(name = "service_id")
  @UpdatePermission(expression = Prefab.NONE)
  public String getServiceId() {
    return serviceId;
  }

  @Column(name = "public")
  @ReadPermission(expression = ReadAccountPrivateDetailsCheck.EXPRESSION)
  public boolean getPublic() {
    return public_;
  }

  public AccountLink setPublic(boolean public_) {
    this.public_ = public_;
    return this;
  }

  @Column(name = "ownership")
  @ReadPermission(expression = ReadAccountPrivateDetailsCheck.EXPRESSION)
  public boolean getOwnership() {
    return ownership;
  }

  @Override
  @Transient
  @JsonIgnore
  public Login getEntityOwner() {
    return user;
  }
}
