package com.faforever.api.user;

import com.faforever.api.data.domain.User;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationEvent;

@Value
@EqualsAndHashCode(callSuper = true)
public class UserUpdatedEvent extends ApplicationEvent {

  int id;

  @NotNull
  String username;

  @NotNull
  String email;

  @NotNull
  String ipAddress;

  public UserUpdatedEvent(@NotNull User source,
                          int id,
                          @NotNull String username,
                          @NotNull String email,
                          @NotNull String ipAddress) {
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
