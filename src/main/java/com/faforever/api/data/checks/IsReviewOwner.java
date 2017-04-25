package com.faforever.api.data.checks;

import com.faforever.api.data.domain.Review;
import com.faforever.api.security.FafUserDetails;
import com.yahoo.elide.security.ChangeSpec;
import com.yahoo.elide.security.RequestScope;
import com.yahoo.elide.security.checks.InlineCheck;

import java.util.Optional;

public class IsReviewOwner {

  public static final String EXPRESSION = "is review owner";

  public static class Inline extends InlineCheck<Review> {

    @Override
    public boolean ok(Review review, RequestScope requestScope, Optional<ChangeSpec> changeSpec) {
      Object opaqueUser = requestScope.getUser().getOpaqueUser();
      return opaqueUser instanceof FafUserDetails
          && review.getPlayer().getId() == ((FafUserDetails) opaqueUser).getId();
    }

    @Override
    public boolean ok(com.yahoo.elide.security.User user) {
      return false;
    }
  }
}
