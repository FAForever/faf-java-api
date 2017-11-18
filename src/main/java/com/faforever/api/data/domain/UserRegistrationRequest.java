package com.faforever.api.data.domain;

import com.faforever.api.data.checks.permission.IsModerator;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.ReadPermission;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "registration_queue")
@Setter
@Include(rootLevel = true, type = "userRegistrationRequest")
@ReadPermission(expression = IsModerator.EXPRESSION)
public class UserRegistrationRequest extends AbstractEntity {
  private String username;
  private String email;
  private Status status;
  private String rejectReason;
  private User reviewer;
  private User createdUser;

  @Column(name = "username")
  public String getUsername() {
    return username;
  }

  @Column(name = "email")
  public String getEmail() {
    return email;
  }

  @Column(name = "status")
  @Enumerated(EnumType.STRING)
  public Status getStatus() {
    return status;
  }

  @Column(name = "reject_reason")
  public String getRejectReason() {
    return rejectReason;
  }

  @ManyToOne
  @JoinColumn(name = "reviewer")
  public User getReviewer() {
    return reviewer;
  }

  @OneToOne
  @JoinColumn(columnDefinition = "created_user")
  public User getCreatedUser() {
    return createdUser;
  }

  public enum Status {
    PENDING,
    APPROVED,
    REJECTED
  }
}
