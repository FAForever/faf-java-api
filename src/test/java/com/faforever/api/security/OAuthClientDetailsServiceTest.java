package com.faforever.api.security;

import com.faforever.api.client.OAuthClient;
import com.faforever.api.client.OAuthClientRepository;
import com.faforever.api.config.FafApiProperties;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientRegistrationException;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OAuthClientDetailsServiceTest {

  private OAuthClientDetailsService instance;

  @Mock
  private OAuthClientRepository oAuthClientRepository;

  @Before
  public void setUp() throws Exception {
    instance = new OAuthClientDetailsService(oAuthClientRepository, new FafApiProperties());
  }

  @Test
  public void loadClientByClientId() throws Exception {
    when(oAuthClientRepository.findOne("123")).thenReturn(new OAuthClient().setDefaultScope(""));

    ClientDetails result = instance.loadClientByClientId("123");

    assertThat(result, notNullValue());
  }

  @Test(expected = ClientRegistrationException.class)
  public void loadClientByClientIdThrowsClientRegistrationExceptionIfNotExists() throws Exception {
    instance.loadClientByClientId("123");
  }
}
