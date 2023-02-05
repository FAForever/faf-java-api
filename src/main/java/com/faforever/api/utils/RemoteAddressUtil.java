package com.faforever.api.utils;

import lombok.experimental.UtilityClass;
import org.springframework.util.Assert;

import jakarta.servlet.http.HttpServletRequest;

@UtilityClass
public class RemoteAddressUtil {
  /** Returns the remote address of the request, considering the {@code X-Forwarded-For} header. */
  public String getRemoteAddress(HttpServletRequest request) {
    Assert.notNull(request, "The parameter 'request' must not be null");

    String remoteAddr = request.getHeader("X-FORWARDED-FOR");
    if (remoteAddr == null || remoteAddr.isEmpty()) {
      remoteAddr = request.getRemoteAddr();
    }

    return remoteAddr;
  }
}
