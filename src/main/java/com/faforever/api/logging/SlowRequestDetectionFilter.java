package com.faforever.api.logging;

import com.faforever.api.config.FafApiProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * This logging interceptor checks for slow http calls and logs the whole request url.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class SlowRequestDetectionFilter implements Filter {

  private final FafApiProperties properties;

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    StopWatch stopWatch = new StopWatch();
    log.trace("Start stopwatch for performance measuring");
    stopWatch.start();

    try {
      chain.doFilter(request, response);
    } finally {
      stopWatch.stop();
      log.trace("Stop stopwatch for performance measuring, total time: {}", stopWatch.getTotalTimeSeconds());

      if (request instanceof HttpServletRequest httpRequest &&
        stopWatch.getTotalTimeSeconds() > properties.getMonitoring().getSlowRequestThresholdSeconds()) {
        StringBuffer urlBuffer = httpRequest.getRequestURL();

        if (StringUtils.hasText(httpRequest.getQueryString())) {
          urlBuffer.append("?");
          urlBuffer.append(httpRequest.getQueryString());
        }

        log.warn("Slow request detected: {} seconds @ {}", stopWatch.getTotalTimeSeconds(), urlBuffer.toString());
      }
    }
  }

}
