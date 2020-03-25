package com.faforever.api.update;

import com.faforever.api.dto.AvatarAssignment;
import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Value;

import java.time.OffsetDateTime;
import java.util.Optional;

@Value
@Type("avatarAssignment")
public class AvatarAssignmentUpdate implements UpdateDto<AvatarAssignment> {
  @Id
  private String id;
  private Optional<Boolean> selected;
  private Optional<OffsetDateTime> expiresAt;
}
