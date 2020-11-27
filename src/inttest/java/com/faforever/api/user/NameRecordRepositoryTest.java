package com.faforever.api.user;

import com.faforever.api.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class NameRecordRepositoryTest extends AbstractIntegrationTest {
  @Autowired
  private NameRecordRepository nameRecordRepository;

  @Test
  void testUsernameReservedOnCurrentUsage() {
    Optional<Integer> result = nameRecordRepository.getLastUsernameOwnerWithinMonths("OLD_MODERATOR", 6);

    assertThat(result.isPresent(), is(true));
  }

  @Test
  void testUsernameReservedOnUsageBehindThreshold() {
    Optional<Integer> result = nameRecordRepository.getLastUsernameOwnerWithinMonths("OLD_ADMIN", 6);

    assertThat(result.isPresent(), is(false));
  }

  @Test
  void testUsernameReservedOnNonExisting() {
    Optional<Integer> result = nameRecordRepository.getLastUsernameOwnerWithinMonths("NOT_EXISTING", 6);

    assertThat(result.isPresent(), is(false));
  }
}


