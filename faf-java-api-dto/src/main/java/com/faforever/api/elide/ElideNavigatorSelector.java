package com.faforever.api.elide;

public interface ElideNavigatorSelector<T extends ElideEntity> {
  ElideNavigatorOnId<T> id(String id);

  ElideNavigatorOnCollection<T> collection();
}
