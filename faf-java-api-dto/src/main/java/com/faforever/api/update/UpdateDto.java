package com.faforever.api.update;

import com.faforever.api.elide.ElideEntity;

public interface UpdateDto<T extends ElideEntity> {
  String getId();
}
