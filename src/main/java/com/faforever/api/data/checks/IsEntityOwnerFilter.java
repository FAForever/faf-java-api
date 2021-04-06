package com.faforever.api.data.checks;

import com.faforever.api.data.domain.Login;
import com.faforever.api.data.domain.OwnableEntity;
import com.faforever.api.error.ProgrammingError;
import com.faforever.api.security.ElideUser;
import com.yahoo.elide.annotation.SecurityCheck;
import com.yahoo.elide.core.Path;
import com.yahoo.elide.core.Path.PathElement;
import com.yahoo.elide.core.filter.Operator;
import com.yahoo.elide.core.filter.expression.FilterExpression;
import com.yahoo.elide.core.filter.predicates.FilterPredicate;
import com.yahoo.elide.core.security.RequestScope;
import com.yahoo.elide.core.security.checks.FilterExpressionCheck;
import com.yahoo.elide.core.type.Field;
import com.yahoo.elide.core.type.Type;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.yahoo.elide.core.type.ClassType.INTEGER_TYPE;

/**
 * Use this filter check on read permissions of whole entities.
 * This check will filter out only entities which have {@link OwnerAttribute} path linked to caller
 * For attributes read permissions and create, update, delete permissions use {@link IsEntityOwner}
 */
public class IsEntityOwnerFilter {

  public static final String EXPRESSION = "is entity owner filter";
  public static final int NON_EXISTING_PLAYER_ID = -1;

  @SecurityCheck(EXPRESSION)
  public static class Filter extends FilterExpressionCheck<OwnableEntity> {

    @Override
    public FilterExpression getFilterExpression(Type<?> entityClass, RequestScope requestScope) {
      final ElideUser caller = (ElideUser) requestScope.getUser();
      final int playerId = caller.getFafId().orElse(NON_EXISTING_PLAYER_ID);
      List<PathElement> pathList = new ArrayList<>();
      getOwnerPath(entityClass, pathList);
      Path path = new Path(pathList);
      return new FilterPredicate(path, Operator.IN, List.of(playerId));
    }

    private void getOwnerPath(Type<?> entity, List<PathElement> ownerPathElements) {
      final Field attribute = Arrays.stream(entity.getDeclaredFields())
        .filter(field -> field.isAnnotationPresent(OwnerAttribute.class))
        .findFirst()
        .orElseThrow(() -> new ProgrammingError("Class [" + entity.getName() + "] does not contain field annotated with @OwnerAttribute"));
      final String attributeName = attribute.getName();
      PathElement ownerPathElement = new PathElement(entity, attribute.getType(), attributeName);
      ownerPathElements.add(ownerPathElement);
      if (attribute.getType().getUnderlyingClass().filter(Login.class::isAssignableFrom).isPresent()) {
        PathElement loginIdPathElement = new PathElement(attribute.getType(), INTEGER_TYPE, "id");
        ownerPathElements.add(loginIdPathElement);
      } else {
        getOwnerPath(attribute.getType(), ownerPathElements);
      }
    }
  }

  /**
   * Use to mark relationship attribute representing Login's subclass
   * or entity having attribute annotated with @OwnerAttribute
   */
  @Target(ElementType.FIELD)
  @Retention(RetentionPolicy.RUNTIME)
  public @interface OwnerAttribute {
  }
}
