package com.faforever.api.data.util;

import com.faforever.api.error.ApiException;
import com.faforever.api.error.ErrorCode;
import com.yahoo.elide.core.security.User;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.MultiValueMap;

import java.util.Optional;

import static com.faforever.api.data.domain.GroupPermission.ROLE_SCRAPER;

@UtilityClass
public class ElidePageSizeUtil {

  static final String PAGE_SIZE_PARAM = "page[size]";
  static final String PAGE_LIMIT_PARAM = "page[limit]";


  public void validatePageSize(final MultiValueMap<String, String> allRequestParams,
    final User principal,
    final int elideDefaultPageSize,
    final int elideMaxPageSize) {

    final int rolePageSizeLimit = getRolePageSizeLimit(principal, elideDefaultPageSize,
      elideMaxPageSize);
    final int pageSizeInRequest = getParamsPageSize(allRequestParams).orElse(elideDefaultPageSize);

    if (pageSizeInRequest > rolePageSizeLimit) {
      throw ApiException.of(ErrorCode.QUERY_INVALID_PAGE_SIZE, pageSizeInRequest,
        rolePageSizeLimit);
    }
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
  private Optional<Integer> getParamsPageSize(MultiValueMap<String, String> allRequestParams) {
    if (allRequestParams.containsKey(PAGE_SIZE_PARAM)) {
      return Optional.ofNullable(allRequestParams.getFirst(PAGE_SIZE_PARAM))
        .filter(StringUtils::isNumeric).map(Integer::parseInt);
    } else if (allRequestParams.containsKey(PAGE_LIMIT_PARAM)) {
      return Optional.ofNullable(allRequestParams.getFirst(PAGE_LIMIT_PARAM))
        .filter(StringUtils::isNumeric).map(Integer::parseInt);
    }
    return Optional.empty();
  }
}
