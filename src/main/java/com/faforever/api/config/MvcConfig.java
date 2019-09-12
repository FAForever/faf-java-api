package com.faforever.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@EnableWebMvc
@Configuration
public class MvcConfig implements WebMvcConfigurer {

  @Override
  public void addViewControllers(ViewControllerRegistry registry) {
    registry.addViewController("/login").setViewName("login");
    registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
    registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    registry.addResourceHandler("/css/style.css").addResourceLocations("/");
    registry.addResourceHandler("/favicon.ico").addResourceLocations("/");
    registry.addResourceHandler("/robots.txt").addResourceLocations("/");
  }

  @Override
  public void configurePathMatch(PathMatchConfigurer configurer) {
    configurer.setUseRegisteredSuffixPatternMatch(true);
  }

  @Override
  public void configureContentNegotiation(final ContentNegotiationConfigurer configurer) {
    // Turn off suffix-based content negotiation
    configurer.favorPathExtension(false);
  }
}
