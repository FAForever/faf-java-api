package com.faforever.api.data;

import com.faforever.api.AbstractIntegrationTest;
import com.faforever.api.data.domain.BanStatus;
import com.faforever.api.email.EmailSender;
import com.faforever.api.player.PlayerRepository;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepDefaultUser.sql")
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepBanRevokeData.sql")
@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sql/cleanBanRevokeData.sql")
public class BanRevokeElideTest extends AbstractIntegrationTest {
  @MockBean
  private EmailSender emailSender;
  @Autowired
  private PlayerRepository playerRepository;
  /*
{"data":{"type":"banRevokeData","attributes":{"reason":"unban"},"relationships":{"ban":{"data":{"type":"banInfo","id":"1"}},"author":{"data":{"type":"player","id":"2"}}}}}   */
  private static final String TEST_REVOKE="{\"data\":{\"type\":\"banRevokeData\",\"attributes\":{\"reason\":\"unban\"},\"relationships\":{\"ban\":{\"data\":{\"type\":\"banInfo\",\"id\":\"1\"}},\"author\":{\"data\":{\"type\":\"player\",\"id\":\"2\"}}}}}";

  @Ignore(value = "Posting of ban revokes never worked, moderator use to change expire date instead see issue #259")
  @WithUserDetails(AUTH_MODERATOR)
  @Test
  public void testRevokeBanWithId1() throws Exception {
    assertThat(playerRepository.getOne(4).getBans().size(), is(1));
    assertThat(playerRepository.getOne(4).getBans().iterator().next().getBanStatus(), is(BanStatus.BANNED));

    mockMvc.perform(post("/data/banRevokeData")
      .content(TEST_REVOKE))
      .andExpect(status().isCreated());

    assertThat(playerRepository.getOne(4).getBans().iterator().next().getBanStatus(), is(BanStatus.DISABLED));
    Mockito.verify(emailSender).sendMail("","","","","");
  }
}
