package com.faforever.api.config.swagger;

import com.faforever.api.config.FafApiProperties;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@Configuration
@RequiredArgsConstructor
public class SwaggerConfig {

  private final FafApiProperties fafApiProperties;

  @Bean
  public OpenAPI fafOpenAPI() {
    return new OpenAPI()
      .servers(Collections.emptyList())
      .info(new Info().title("Forged Alliance Forever API")
        .description("The official API for Forged Alliance Forever")
        .version(fafApiProperties.getVersion())
        .license(new License()
          .name("MIT")
          .url("https://github.com/FAForever/faf-java-api/blob/develop/LICENSE")
        )
        .contact(new Contact()
          .name("Admin")
          .url("https://github.com/FAForever/faf-java-api")
          .email("admin@faforever.com")
        )
      );
  }
}
