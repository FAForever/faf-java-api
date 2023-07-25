package com.faforever.api.config.elide;

import com.faforever.api.data.DataController;
import com.yahoo.elide.core.dictionary.EntityDictionary;
import com.yahoo.elide.swagger.OpenApiBuilder;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
public class ElideSwaggerController {

  private EntityDictionary entityDictionary;

  @Autowired
  public ElideSwaggerController(EntityDictionary entityDictionary) {
    this.entityDictionary = entityDictionary;
  }

  @Hidden
  @GetMapping(value = "/elide/docs", produces = {APPLICATION_JSON_VALUE})
  public ResponseEntity<OpenAPI> getDocumentation() {
    OpenAPI openAPI = new OpenApiBuilder(entityDictionary)
      .basePath(DataController.PATH_PREFIX)
      .build();

    openAPI.info(new Info().title("Elide JSON API"));

    return ResponseEntity.ok(openAPI);
  }
}
