package com.faforever.api;

import com.faforever.api.config.ElideTestConfig;
import com.faforever.api.error.ErrorCode;
import com.faforever.api.utils.OAuthHelper;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.internal.util.collections.Sets;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.transaction.Transactional;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration-test")
@Import({ElideTestConfig.class, OAuthHelper.class})
@Transactional
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepDefaultUser.sql")
public abstract class AbstractIntegrationTest {
  protected final static String AUTH_USER = "USER";
  protected final static String AUTH_MODERATOR = "MODERATOR";
  protected final static String AUTH_ADMIN = "ADMIN";
  @Autowired
  protected OAuthHelper oAuthHelper;
  protected MockMvc mockMvc;
  @Autowired
  protected WebApplicationContext context;

  @Before
  public void setUp() {
    this.mockMvc = MockMvcBuilders
      .webAppContextSetup(this.context)
      .apply(springSecurity())
      .build();
  }

  protected RequestPostProcessor getOAuthToken(String... scope) {
    return oAuthHelper.addBearerToken(Sets.newSet(scope));
  }

  protected void assertApiError(MvcResult mvcResult, ErrorCode errorCode) throws Exception {
    JSONObject resonseJson = new JSONObject(mvcResult.getResponse().getContentAsString());
    JSONAssert.assertEquals(String.format("{\"errors\":[{\"code\":\"%s\"}]}", errorCode.getCode()), resonseJson, false);
  }
}
