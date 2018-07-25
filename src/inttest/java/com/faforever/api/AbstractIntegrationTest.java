package com.faforever.api;

import com.faforever.api.config.ApplicationProfile;
import com.faforever.api.error.ErrorCode;
import com.faforever.api.utils.OAuthHelper;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
@ActiveProfiles(ApplicationProfile.INTEGRATION_TEST)
@Import(OAuthHelper.class)
@Transactional
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepDefaultUser.sql")
public abstract class AbstractIntegrationTest {
  protected final static String AUTH_WEBSITE = "WEBSITE";
  protected final static String AUTH_USER = "USER";
  protected final static String AUTH_MODERATOR = "MODERATOR";
  protected final static String AUTH_ADMIN = "ADMIN";
  @Autowired
  protected OAuthHelper oAuthHelper;
  protected MockMvc mockMvc;
  @Autowired
  protected WebApplicationContext context;
  protected ObjectMapper objectMapper;

  @Before
  public void setUp() {
    this.mockMvc = MockMvcBuilders
      .webAppContextSetup(this.context)
      .apply(springSecurity())
      .build();

    this.objectMapper = new ObjectMapper();
    this.objectMapper.registerModule(new JavaTimeModule());
    this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    this.objectMapper.setSerializationInclusion(Include.NON_EMPTY);
  }

  protected RequestPostProcessor getOAuthToken(String... scope) {
    return oAuthHelper.addBearerToken(Sets.newSet(scope));
  }

  protected void assertApiError(MvcResult mvcResult, ErrorCode errorCode) throws Exception {
    JSONObject resonseJson = new JSONObject(mvcResult.getResponse().getContentAsString());
    JSONAssert.assertEquals(String.format("{\"errors\":[{\"code\":\"%s\"}]}", errorCode.getCode()), resonseJson, false);
  }
}
