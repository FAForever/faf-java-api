package com.faforever.api.config.swagger;

import com.faforever.api.config.FafApiProperties;
import com.google.common.base.Predicate;
import io.swagger.annotations.Api;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.inject.Inject;

import static com.google.common.base.Predicates.or;
import static springfox.documentation.builders.PathSelectors.regex;

@Configuration
@EnableSwagger2
@Api(value = "Data API", tags = {"foo", "bar"})
public class SwaggerConfig {

  private final FafApiProperties fafApiProperties;

  @Inject
  public SwaggerConfig(FafApiProperties fafApiProperties) {
    this.fafApiProperties = fafApiProperties;
  }

  @Bean
  public Docket newsApi() {
    return new Docket(DocumentationType.SWAGGER_2)
      .apiInfo(apiInfo())
      .select().paths(paths())
      .build();
  }

  private ApiInfo apiInfo() {
    return new ApiInfoBuilder()
      .title("Forged Alliance Forever API")
      .description("The official API for Forged Alliance Forever")
      .contact(new Contact("Downlord", "https://github.com/FAForever/faf-java-api", "downlord@faforever.com"))
      .license("MIT")
      .licenseUrl("https://github.com/FAForever/faf-java-api/blob/develop/LICENSE")
      .version(fafApiProperties.getVersion())
      .build();
  }

  private Predicate<String> paths() {
    return or(
      regex("/oauth/(.*token.*|.*authorize)"),
      regex("/data/.*"),
      regex("/health.*"),
      regex("/clans/.*"),
      regex("/achievements/.*"),
      regex("/avatars/.*"),
      regex("/events/.*"),
      regex("/users/.*"),
      regex("/mods/.*"),
      regex("/maps/.*"),
      regex("/exe/.*"),
      regex("/leaderboards/.*"),
      regex("/voting/.*"));
  }
}
