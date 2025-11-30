package com.faforever.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.HttpMessageConverters;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
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
    registry
      .addResourceHandler("/**")
      .addResourceLocations("classpath:/META-INF/resources/");
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
      .allowedMethods("*");
  }

  @Override
  public void configureMessageConverters(HttpMessageConverters.ServerBuilder builder) {
    builder.addCustomConverter(new IgnoreOctetStreamToObjectHttpMessageConverter());
  }

  @Override
  public void addFormatters(FormatterRegistry registry) {
    registry.addConverter(new NoopMultipartFileToStringConverter());
  }
}
