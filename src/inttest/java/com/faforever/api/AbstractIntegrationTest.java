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
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
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
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Set;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles(ApplicationProfile.INTEGRATION_TEST)
@Import(OAuthHelper.class)
@Transactional
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepDefaultUser.sql")
public abstract class AbstractIntegrationTest {
  protected static final String NO_SCOPE = "no_scope";
  protected static final String NO_AUTHORITIES = "NO_AUTHORITIES";
  protected static final DateTimeFormatter OFFSET_DATE_TIME_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

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
  protected ResourceConverter resourceConverter;

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

  protected RequestPostProcessor getOAuthTokenWithoutUser(String... scope) {
    return oAuthHelper.addBearerToken(Sets.newSet(scope), null);
  }

  protected RequestPostProcessor getOAuthTokenWithTestUser(String scope, String authority) {
    return getOAuthTokenWithTestUser(Collections.singleton(scope), Collections.singleton(authority));
  }

  protected RequestPostProcessor getOAuthTokenWithTestUser(Set<String> scope, Set<String> authorities) {
    return oAuthHelper.addBearerToken(5, "ACTIVE_USER", scope, authorities);
  }

  protected void assertApiError(MvcResult mvcResult, ErrorCode errorCode) throws Exception {
    JSONObject resonseJson = new JSONObject(mvcResult.getResponse().getContentAsString());
    JSONAssert.assertEquals(String.format("{\"errors\":[{\"code\":\"%s\"}]}", errorCode.getCode()), resonseJson, false);
  }

  protected <T extends AbstractEntity> byte[] createJsonApiContent(T entity) throws DocumentSerializationException {
    return resourceConverter.writeDocument(new JSONAPIDocument<>(entity));
  }
}
