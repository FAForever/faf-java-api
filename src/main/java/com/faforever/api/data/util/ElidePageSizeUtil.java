package com.faforever.api.data.util;

import com.yahoo.elide.core.security.User;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Optional;

import static com.faforever.api.data.domain.GroupPermission.ROLE_SCRAPER;

@UtilityClass
@Slf4j
public class ElidePageSizeUtil {

  static final String PAGE_SIZE_PARAM = "page[size]";
  static final String PAGE_LIMIT_PARAM = "page[limit]";


  public void validateAndAdjustPageSize(
    final MultiValueMap<String, String> allRequestParamsMutable,
    final User principal,
    final int elideDefaultPageSize,
    final int elideMaxPageSize) {

    final int rolePageSizeLimit = getRolePageSizeLimit(principal, elideDefaultPageSize,
      elideMaxPageSize);

    List.of(PAGE_SIZE_PARAM, PAGE_LIMIT_PARAM).forEach(
      paramName -> doValidateAndAdjustPageSize(allRequestParamsMutable, rolePageSizeLimit,
        paramName));
  }

  private int getRolePageSizeLimit(final User principal, final int elideDefaultPageSize,
    final int elideMaxPageSize) {
    if (principal.isInRole(ROLE_SCRAPER)) {
      return elideMaxPageSize;
    }
    return elideDefaultPageSize;
  }

  /**
   * @implNote Invalid numbers should be handled by Elide. That's why they are filtered out with
   * {@link StringUtils::isNumeric}
   */
  private void doValidateAndAdjustPageSize(
    MultiValueMap<String, String> allRequestParamsMutable, int rolePageSizeLimit,
    String paramName) {
    Optional.ofNullable(allRequestParamsMutable.getFirst(paramName))
      .filter(StringUtils::isNumeric).map(Integer::parseInt)
      .ifPresent(requestPageSize -> {
        if (requestPageSize > rolePageSizeLimit) {
          allRequestParamsMutable.set(paramName, String.valueOf(rolePageSizeLimit));
          log.debug(
            "Request page size param ['{}'='{}'] value is bigger that allowed '{}'. Overriding value",
            paramName,
            requestPageSize,
            rolePageSizeLimit
          );
        }
      });
  }
}
