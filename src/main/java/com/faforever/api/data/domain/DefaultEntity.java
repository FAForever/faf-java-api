package com.faforever.api.data.domain;

import java.time.OffsetDateTime;

public interface DefaultEntity {
  Integer getId();

  OffsetDateTime getCreateTime();

  OffsetDateTime getUpdateTime();
}
