package com.faforever.integration.factories;

import com.faforever.api.data.domain.Avatar;
import com.faforever.api.data.domain.AvatarAssignment;
import com.faforever.api.data.domain.Player;
import com.faforever.integration.TestDatabase;
import lombok.Builder;
import org.springframework.util.Assert;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class AvatarFactory {
  public static final String DEFAULT_TOOLTIP = "Cool Test Avatar Tooltip";
  public static final String DEFAULT_URL = "https://www.faf-test.org/myavatar.png";

  @Builder
  public static Avatar createAvatar(String tooltip,
                                    String url,
                                    Player player,
                                    TestDatabase database) {
    Assert.notNull(database, "'database' must not be null");
    Avatar avatar = new Avatar()
      .setTooltip(tooltip != null ? tooltip : DEFAULT_TOOLTIP)
      .setUrl(url != null ? url : DEFAULT_URL);

    long assignmentCount = database.getAvatarAssignmentRepository().count();
    if (player != null) {
      AvatarAssignment assignment = new AvatarAssignment()
        .setAvatar(avatar)
        .setPlayer(player);
      avatar.setAssignments(Collections.singletonList(assignment));
      assignmentCount++;
    }

    long count = database.getAvatarRepository().count();
    database.getAvatarRepository().save(avatar);
    assertEquals(count + 1, database.getAvatarRepository().count());
    assertEquals(assignmentCount, database.getAvatarAssignmentRepository().count());
    return avatar;
  }
}
