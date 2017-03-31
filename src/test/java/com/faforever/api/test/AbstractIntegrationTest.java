package com.faforever.api.test;

import com.faforever.api.client.ClientType;
import com.faforever.api.client.OAuthClient;
import com.faforever.api.client.OAuthClientRepository;
import com.faforever.api.data.domain.User;
import com.faforever.api.user.UserRepository;
import org.junit.Before;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordResourceDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Class to be extended by all integration tests. Starts the Spring context and executes each test within a transaction
 * so that all data is rolled back afterwards. Also, the spring profile "integration" is activated.
 * <p>
 * FIXME this has not yet been tested an needs some more work.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Category(IntegrationTest.class)
@Transactional
@ActiveProfiles("integration")
public abstract class AbstractIntegrationTest {

  private final ShaPasswordEncoder shaPasswordEncoder;
  protected OAuth2RestTemplate restTemplate;
  @Autowired
  private OAuthClientRepository oAuthClientRepository;
  @Autowired
  private UserRepository userRepository;
  @Value("${local.server.port}")
  private int port;

  protected AbstractIntegrationTest() {
    this.shaPasswordEncoder = new ShaPasswordEncoder(256);
  }

  @Before
  public void setUp() throws Exception {
  }


  protected void authenticate(String clientId, String clientSecret, String username, String password) {
    oAuthClientRepository.save(new OAuthClient()
        .setId(clientId)
        .setName("test")
        .setClientSecret(clientSecret)
        .setRedirectUris("http://localhost")
        .setDefaultRedirectUri("http://localhost")
        .setDefaultScope("")
        .setClientType(ClientType.PUBLIC));

    User user = (User) new User()
        .setPassword(shaPasswordEncoder.encodePassword(password, null))
        .setLogin(username)
        .setEMail(username + "@faforever.com");
    userRepository.save(user);

    String baseUri = "http://localhost:" + port;

    ResourceOwnerPasswordResourceDetails resourceDetails = new ResourceOwnerPasswordResourceDetails();
    resourceDetails.setClientId(clientId);
    resourceDetails.setClientSecret(clientSecret);
    resourceDetails.setAccessTokenUri(baseUri + "/oauth/token");
    resourceDetails.setUsername(username);
    resourceDetails.setPassword(password);

    restTemplate = new RestTemplateBuilder()
        .rootUri(baseUri)
        .basicAuthorization(clientId, clientSecret)
        .configure(new OAuth2RestTemplate(resourceDetails));
  }
}
