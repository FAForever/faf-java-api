package com.faforever.api.user;

import com.faforever.api.data.domain.User;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
@EqualsAndHashCode(callSuper = true)
public class UserUpdatedEvent extends ApplicationEvent {
  private final int id;
  private final String username;
  private final String email;
  private final String ipAddress;

  public UserUpdatedEvent(User source, int id, String username, String email, String ipAddress) {
    super(source);
    this.id = id;
    this.username = username;
    this.email = email;
    this.ipAddress = ipAddress;
  }

  @Override
  public User getSource() {
    return (User) super.getSource();
  }
}
