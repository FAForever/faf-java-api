package com.faforever.api.security;

import com.faforever.api.client.OAuthClient;
import com.faforever.api.client.OAuthClientRepository;
import com.faforever.api.config.FafApiProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientRegistrationException;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OAuthClientDetailsServiceTest {

  private OAuthClientDetailsService instance;

  @Mock
  private OAuthClientRepository oAuthClientRepository;

  @BeforeEach
  public void setUp() throws Exception {
    instance = new OAuthClientDetailsService(oAuthClientRepository, new FafApiProperties());
  }

  @Test
  public void loadClientByClientId() throws Exception {
    when(oAuthClientRepository.findById("123")).thenReturn(Optional.of(new OAuthClient().setDefaultScope("").setRedirectUris("")));

    ClientDetails result = instance.loadClientByClientId("123");

    assertThat(result, notNullValue());
  }

  @Test
  public void loadClientByClientIdThrowsClientRegistrationExceptionIfNotExists() throws Exception {
    Assertions.assertThrows( ClientRegistrationException.class, () -> instance.loadClientByClientId("123") );
  }
}
