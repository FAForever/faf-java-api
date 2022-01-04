package com.faforever.api.config.elide;

import com.faforever.api.data.DataController;
import com.yahoo.elide.core.dictionary.EntityDictionary;
import com.yahoo.elide.swagger.SwaggerBuilder;
import io.swagger.models.Info;
import io.swagger.models.Swagger;
import io.swagger.v3.oas.annotations.Hidden;
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
  public ResponseEntity<String> getDocumentation() {
    Info info = new Info().title("Elide JSON API").version("");
    SwaggerBuilder builder = new SwaggerBuilder(entityDictionary, info);
    Swagger document = builder.build()
      .basePath(DataController.PATH_PREFIX);

    return ResponseEntity.ok(SwaggerBuilder.getDocument(document));
  }
}
