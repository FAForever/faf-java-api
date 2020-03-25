package com.faforever.api.elide;

public interface ElideNavigatorOnId<T extends ElideEntity> {

  ElideNavigatorOnId<T> addIncludeOnId(String include);

  <R extends ElideEntity> ElideNavigatorSelector<R> navigateRelationship(Class<R> entityClass, String name);

  Class<T> getDtoClass();

  String build();
}
