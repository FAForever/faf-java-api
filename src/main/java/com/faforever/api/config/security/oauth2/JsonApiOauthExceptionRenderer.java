package com.faforever.api.config.security.oauth2;

import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.security.oauth2.http.converter.jaxb.JaxbOAuth2ExceptionMessageConverter;
import org.springframework.security.oauth2.provider.error.DefaultOAuth2ExceptionRenderer;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

public class JsonApiOauthExceptionRenderer extends DefaultOAuth2ExceptionRenderer {
  public JsonApiOauthExceptionRenderer() {
    setMessageConverters(createMessageConverters());
  }

  private List<HttpMessageConverter<?>> createMessageConverters() {
    List<HttpMessageConverter<?>> result = new ArrayList<HttpMessageConverter<?>>();
    result.add(new JsonApiOAuthMessageConverter());
    result.addAll(new RestTemplate().getMessageConverters());
    result.add(new JaxbOAuth2ExceptionMessageConverter());
    return result;
  }
}
