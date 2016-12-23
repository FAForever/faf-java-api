package com.faforever.api.swagger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import static springfox.documentation.builders.PathSelectors.regex;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

  @Bean
  public Docket newsApi() {
    return new Docket(DocumentationType.SWAGGER_2)
        .apiInfo(apiInfo())
        .select()
        .paths(regex("/greeting.*"))
        .build();
  }

  private ApiInfo apiInfo() {
    return new ApiInfoBuilder()
        .title("Forged Alliance Forever API (Prototype)")
        .description("A prototype of a Java-based API for Forged Alliance Forever")
        .contact(new Contact("Michel Jung", "https://github.com/micheljung", "info@micheljung.ch"))
        .license("MIT")
        .licenseUrl("https://github.com/micheljung/faf-java-api/blob/develop/LICENSE")
        .version("2.0")
        .build();
  }
}
