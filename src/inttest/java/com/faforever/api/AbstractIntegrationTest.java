package com.faforever.api;

import com.faforever.api.config.ApplicationProfile;
import com.faforever.api.error.ErrorCode;
import com.faforever.api.utils.OAuthHelper;
import com.faforever.commons.api.dto.AbstractEntity;
import com.faforever.commons.api.dto.Avatar;
import com.faforever.commons.api.dto.AvatarAssignment;
import com.faforever.commons.api.dto.BanInfo;
import com.faforever.commons.api.dto.DomainBlacklist;
import com.faforever.commons.api.dto.ModerationReport;
import com.faforever.commons.api.dto.Player;
import com.faforever.commons.api.dto.Tutorial;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.transaction.Transactional;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@Testcontainers
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles(ApplicationProfile.INTEGRATION_TEST)
@Import(OAuthHelper.class)
@Transactional
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/truncateTables.sql")
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepDefaultData.sql")
public abstract class AbstractIntegrationTest {

  protected static final String NO_SCOPE = "no_scope";
  protected static final String NO_AUTHORITIES = "NO_AUTHORITIES";
  protected static final DateTimeFormatter OFFSET_DATE_TIME_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

  protected final static String AUTH_WEBSITE = "WEBSITE";
  protected final static String AUTH_USER = "USER";
  protected final static String AUTH_MODERATOR = "MODERATOR";
  protected final static String AUTH_ADMIN = "ADMIN";
  protected final static String AUTH_ACTIVE_USER = "ACTIVE_USER";

  protected final static int USERID_USER = 1;
  protected final static int USERID_MODERATOR = 2;
  protected final static int USERID_ADMIN = 3;
  protected final static int USERID_BANNED = 4;
  protected final static int USERID_ACTIVE_USER = 5;

  @Autowired
  protected OAuthHelper oAuthHelper;
  protected MockMvc mockMvc;
  @Autowired
  protected WebApplicationContext context;
  @Autowired
  protected ObjectMapper objectMapper;
  protected ResourceConverter resourceConverter;

  @BeforeEach
  public void setUp() {
    this.mockMvc = MockMvcBuilders
      .webAppContextSetup(this.context)
      .apply(springSecurity())
      .build();

    this.objectMapper.setSerializationInclusion(Include.NON_EMPTY);

    // maybe use reflections library to find all subtypes of AbstractEntity
    resourceConverter = new ResourceConverter(
      objectMapper,
      ModerationReport.class,
      Player.class,
      Tutorial.class,
      Avatar.class,
      AvatarAssignment.class,
      BanInfo.class,
      DomainBlacklist.class
    );
  }


  protected RequestPostProcessor getOAuthTokenWithActiveUser(String scope, String authority) {
    return getOAuthTokenWithActiveUser(Set.of(scope), Set.of(authority));
  }

  protected RequestPostProcessor getOAuthTokenWithActiveUser(Set<String> scopes, Set<String> authorities) {
    return oAuthHelper.addBearerToken(5, scopes, authorities);
  }

  protected RequestPostProcessor getOAuthTokenForUserId(int userId, String... scopes) {
    return oAuthHelper.addBearerTokenForUser(userId, Arrays.stream(scopes).collect(Collectors.toSet()));
  }

  protected void assertApiError(MvcResult mvcResult, ErrorCode errorCode) throws Exception {
    JSONObject resonseJson = new JSONObject(mvcResult.getResponse().getContentAsString());
    JSONAssert.assertEquals(String.format("{\"errors\":[{\"code\":\"%s\"}]}", errorCode.getCode()), resonseJson, false);
  }

  protected <T extends AbstractEntity> byte[] createJsonApiContent(T entity) throws DocumentSerializationException {
    return resourceConverter.writeDocument(new JSONAPIDocument<>(entity));
  }
}
