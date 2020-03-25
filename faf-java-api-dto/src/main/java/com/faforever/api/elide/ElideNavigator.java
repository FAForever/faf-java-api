package com.faforever.api.elide;

import com.faforever.api.update.UpdateDto;
import com.github.jasminb.jsonapi.annotations.Type;
import com.github.rutledgepaulv.qbuilders.builders.QBuilder;
import com.github.rutledgepaulv.qbuilders.conditions.Condition;
import com.github.rutledgepaulv.qbuilders.visitors.RSQLVisitor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/***
 * A utility class to build JSON-API / Elide-compatible URLs with include, filtering and sorting
 * @param <T>
 */
@Slf4j
public class ElideNavigator<T extends ElideEntity> implements ElideNavigatorSelector<T>, ElideNavigatorOnId<T>, ElideNavigatorOnCollection<T> {
  private Optional<String> id = Optional.empty();
  @Getter
  private Class<T> dtoClass;
  private Optional<ElideNavigator<?>> parentNavigator;
  private String parentName = null;
  private Optional<String> relationship = Optional.empty();
  private List<String> includes = new ArrayList<>();
  private List<String> sorts = new ArrayList<>();
  private Optional<Condition<?>> filterCondition = Optional.empty();
  private Optional<Integer> pageSize = Optional.empty();
  private Optional<Integer> pageNumber = Optional.empty();

  private ElideNavigator(@NotNull Class<T> dtoClass) {
    this.dtoClass = dtoClass;
    this.parentNavigator = Optional.empty();
  }

  private ElideNavigator(@NotNull Class<T> dtoClass, @NotNull ElideNavigator<?> parentNavigator, @NotNull String parentName) {
    this.dtoClass = dtoClass;
    this.parentNavigator = Optional.of(parentNavigator);
    this.parentName = parentName;
  }

  public static <T extends QBuilder<T>> QBuilder<T> qBuilder() {
    return new QBuilder<>();
  }

  /**
   * Start a navigator for given type
   */
  public static <T extends ElideEntity> ElideNavigatorSelector<T> of(@NotNull Class<T> dtoClass) {
    return new ElideNavigator<>(dtoClass);
  }

  /**
   * Build a ElideNavigator directed to the given entity
   */
  public static <T extends ElideEntity> ElideNavigatorOnId<T> of(@NotNull T entity) {
    //noinspection unchecked
    return new ElideNavigator<>((Class<T>) entity.getClass()).id(entity.getId());
  }

  /**
   * Build a ElideNavigator directed to the given entity
   */
  public static <T extends ElideEntity> ElideNavigatorOnId<T> of(@NotNull UpdateDto<T> entity) {
    //noinspection unchecked
    return new ElideNavigator<>((Class<T>) entity.getClass()).id(entity.getId());
  }

  /**
   * Point to a certain id of entity type T
   */
  @Override
  public ElideNavigatorOnId<T> id(@NotNull String id) {
    this.id = Optional.of(id);
    return this;
  }

  /**
   * Point to a collection of type T
   *
   * @return
   */
  @Override
  public ElideNavigatorOnCollection<T> collection() {
    return this;
  }

  /**
   * Add an include to an ID-pointed ElideNavigator Important: ElideNavigator takes care of referencing to the correct
   * parent relationships. Just use a relative include.
   */
  @Override
  public ElideNavigatorOnId<T> addIncludeOnId(@NotNull String include) {
    return addInclude(include);
  }

  private ElideNavigator<T> addInclude(@NotNull String include) {
    log.trace("include added: {}", include);
    includes.add(include);
    return this;
  }

  @Override
  public <R extends ElideEntity> ElideNavigatorSelector<R> navigateRelationship(@NotNull Class<R> dtoClass, @NotNull String name) {
    log.trace("relationship added: {}", name);
    this.relationship = Optional.of(name);
    return new ElideNavigator<>(dtoClass, this, name);
  }

  /**
   * Add an include to a collection-pointed ElideNavigator Important: ElideNavigator takes care of referencing to the
   * correct parent relationships. Just use a relative include.
   */
  @Override
  public ElideNavigatorOnCollection<T> addIncludeOnCollection(@NotNull String include) {
    return addInclude(include);
  }

  /**
   * Add a sorting rule to the navigator Important: You need to give the full qualified route, there is NO referencing
   * of parent relationships. This is due to the fact that you need full control over the order of the sorting.
   */
  @Override
  public ElideNavigatorOnCollection<T> addSortingRule(@NotNull String field, boolean ascending) {
    log.trace("{} sort added: {}", ascending ? "ascending" : "descending", field);
    sorts.add((ascending ? "+" : "-") + field);
    return this;
  }

  /**
   * Add a filter to a collection-pointed ElideNavigator Important: ElideNavigator takes care of referencing to the
   * correct parent relationships. Just use a relative include.
   */
  @Override
  public ElideNavigatorOnCollection<T> addFilter(@NotNull Condition<?> eq) {
    log.trace("filter set: {}", eq.toString());
    filterCondition = Optional.of(eq);
    return this;
  }

  @Override
  public ElideNavigatorOnCollection<T> pageSize(int size) {
    log.trace("page size set: {}", size);
    pageSize = Optional.of(size);
    return this;
  }

  @Override
  public ElideNavigatorOnCollection<T> pageNumber(int number) {
    log.trace("page number set: {}", number);
    pageNumber = Optional.of(number);
    return this;
  }

  private String pathToRoot() {
    return parentNavigator.map(parent -> parent.pathToRoot() + parent.parentName + ".").orElse("");
  }

  @Override
  public String build() {
    String dtoPath = dtoClass.getDeclaredAnnotation(Type.class).value();

    StringJoiner queryArgs = new StringJoiner("&", "?", "")
      .setEmptyValue("");

    if (includes.size() > 0) {
      queryArgs.add(String.format("include=%s", includes.stream()
        .map(s -> pathToRoot() + s)
        .collect(Collectors.joining(","))));
    }

    filterCondition.ifPresent(cond -> queryArgs.add(String.format("filter=%s", cond.query(new RSQLVisitor()))));

    if (sorts.size() > 0) {
      queryArgs.add(String.format("sort=%s", sorts.stream()
        .collect(Collectors.joining(","))));
    }

    pageSize.ifPresent(i -> queryArgs.add(String.format("page[size]=%s", i)));
    pageNumber.ifPresent(i -> queryArgs.add(String.format("page[number]=%s", i)));

    String route = parentNavigator.map(ElideNavigator::build)
      .orElse("/data/" + dtoPath) +
      id.map(i -> "/" + i).orElse("") +
      relationship.map(r -> "/" + r).orElse("") +
      queryArgs.toString();
    log.debug("Route built: {}", route);
    return route;
  }
}
