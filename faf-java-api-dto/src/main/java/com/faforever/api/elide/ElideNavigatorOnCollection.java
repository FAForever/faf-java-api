package com.faforever.api.elide;

import com.github.rutledgepaulv.qbuilders.conditions.Condition;

public interface ElideNavigatorOnCollection<T extends ElideEntity> {

  ElideNavigatorOnCollection<T> addIncludeOnCollection(String include);

  ElideNavigatorOnCollection<T> addSortingRule(String field, boolean ascending);

  ElideNavigatorOnCollection<T> addFilter(Condition<?> eq);

  ElideNavigatorOnCollection<T> pageSize(int size);

  ElideNavigatorOnCollection<T> pageNumber(int number);

  Class<T> getDtoClass();

  String build();
}
