package com.faforever.api.user;

import com.faforever.api.data.domain.User;
import com.faforever.api.error.ErrorCode;
import com.faforever.api.security.OAuthScope;
import com.faforever.integration.OAuthHelper;
import com.google.common.collect.Sets;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;

import javax.transaction.Transactional;
import java.util.Collections;

import static junitx.framework.Assert.assertEquals;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration-test")
@Import(OAuthHelper.class)
@Transactional
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/createUsers.sql")
public class UserControllerTest {
  protected final static String AUTH_USER = "USER";
  protected final static String AUTH_MODERATOR = "MODERATOR";
  protected final static String AUTH_ADMIN = "ADMIN";
  MockMvc mockMvc;
  @Autowired
  private WebApplicationContext context;
  @Autowired
  private OAuthHelper oAuthHelper;

  @Autowired
  private UserRepository userRepository;

  @Before
  public void setUp() {
    this.mockMvc = MockMvcBuilders
      .webAppContextSetup(this.context)
      .apply(springSecurity())
      .build();
  }

  void assertApiError(MvcResult mvcResult, ErrorCode errorCode) throws Exception {
    JSONObject resonseJson = new JSONObject(mvcResult.getResponse().getContentAsString());
    JSONAssert.assertEquals(String.format("{\"errors\":[{\"code\":\"%s\"}]}", errorCode.getCode()), resonseJson, false);
  }

  @Test
  @WithUserDetails(AUTH_USER)
  public void changePasswordWithSuccess() throws Exception {
    MultiValueMap<String, String> params = new HttpHeaders();
    params.add("currentPassword", AUTH_USER);
    params.add("newPassword", "newPassword");

    RequestPostProcessor oauthToken = oAuthHelper.addBearerToken(Sets.newHashSet(OAuthScope._WRITE_ACCOUNT_DATA));
    mockMvc.perform(post("/users/changePassword").with(oauthToken).params(params))
      .andExpect(status().isOk());

    User user = userRepository.findOneByLoginIgnoreCase(AUTH_USER).get();
    assertEquals(user.getPassword(), "5c29a959abce4eda5f0e7a4e7ea53dce4fa0f0abbe8eaa63717e2fed5f193d31");
  }

  @Test
  @WithUserDetails(AUTH_USER)
  public void changePasswordWithWrongScope() throws Exception {
    MultiValueMap<String, String> params = new HttpHeaders();
    params.add("currentPassword", AUTH_USER);
    params.add("newPassword", "newPassword");

    RequestPostProcessor oauthToken = oAuthHelper.addBearerToken(Collections.emptySet());
    mockMvc.perform(post("/users/changePassword").with(oauthToken).params(params))
      .andExpect(status().isForbidden());
  }

  @Test
  @WithUserDetails(AUTH_USER)
  public void changePasswordWithWrongPassword() throws Exception {
    MultiValueMap<String, String> params = new HttpHeaders();
    params.add("currentPassword", "wrongPassword");
    params.add("newPassword", "newPassword");

    RequestPostProcessor oauthToken = oAuthHelper.addBearerToken(Sets.newHashSet(OAuthScope._WRITE_ACCOUNT_DATA));
    MvcResult mvcResult = mockMvc.perform(post("/users/changePassword").with(oauthToken).params(params))
      .andExpect(status().is4xxClientError())
      .andReturn();

    assertApiError(mvcResult, ErrorCode.PASSWORD_CHANGE_FAILED_WRONG_PASSWORD);
  }
}
