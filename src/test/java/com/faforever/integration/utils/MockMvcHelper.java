package com.faforever.integration.utils;

import com.faforever.integration.factories.SessionFactory.Session;
import lombok.Setter;
import lombok.SneakyThrows;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

public class MockMvcHelper {

  public static MockMvcHelperObject of(MockMvc mvc) {
    return new MockMvcHelperObject(mvc);
  }


  public static class MockMvcHelperObject {
    private MockMvc mvc;
    @Setter
    private Session session;

    private MockMvcHelperObject(MockMvc mvc) {
      this.mvc = mvc;
    }

    @SneakyThrows
    public ResultActions perform(MockHttpServletRequestBuilder requestBuilder) {
      MockHttpServletRequestBuilder newBuilder = requestBuilder;
      if (session != null) {
        newBuilder = requestBuilder.header("Authorization", session.getToken());
      }
      return mvc.perform(newBuilder);
    }
  }
}
