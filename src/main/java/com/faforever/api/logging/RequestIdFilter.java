package com.faforever.api.logging;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.UUID;

/**
 * This logging interceptor sets up the MDC with the external request id (if present) or a random UUID otherwise.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class RequestIdFilter implements Filter {

  /**
   * Use this key in your log pattern and for fetching they request id from MDC.
   */
  public static final String REQUEST_ID_KEY = "requestId";
  /**
   * The default request id header. Change this if you configured a different key.
   */
  private static final String REQUEST_ID_HEADER = "X-Request-ID";

  @Override
  public void init(FilterConfig filterConfig) {
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    String requestId = null;

    if (request instanceof HttpServletRequest) {
      HttpServletRequest httpRequest = (HttpServletRequest) request;
      requestId = httpRequest.getHeader(REQUEST_ID_HEADER);
    }

    if (StringUtils.isBlank(requestId)) {
      requestId = UUID.randomUUID().toString();
      log.trace("No header value found for key '{}'. Fallback to random UUID: {}", REQUEST_ID_HEADER, requestId);
    } else {
      log.trace("Request id read from header key '{}': {}", REQUEST_ID_HEADER, requestId);
    }

    MDC.put(REQUEST_ID_KEY, requestId);
    log.trace("Adding request id key '{}' to logging context", REQUEST_ID_KEY);

    try {
      chain.doFilter(request, response);
    } finally {
      log.trace("Removing request id key '{}' from logging context", REQUEST_ID_KEY);
      MDC.remove(REQUEST_ID_KEY);
    }
  }

  @Override
  public void destroy() {
  }

}
