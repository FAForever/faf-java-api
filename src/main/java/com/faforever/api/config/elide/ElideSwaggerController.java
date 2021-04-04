package com.faforever.api.config.elide;

import com.faforever.api.data.DataController;
import com.yahoo.elide.core.dictionary.EntityDictionary;
import com.yahoo.elide.swagger.SwaggerBuilder;
import io.swagger.models.Info;
import io.swagger.models.Swagger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;
import springfox.documentation.spring.web.json.Json;
import springfox.documentation.spring.web.json.JsonSerializer;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
public class ElideSwaggerController {

  private JsonSerializer jsonSerializer;
  private EntityDictionary entityDictionary;

  @Autowired
  public ElideSwaggerController(JsonSerializer jsonSerializer, EntityDictionary entityDictionary) {
    this.jsonSerializer = jsonSerializer;
    this.entityDictionary = entityDictionary;
  }

  @ApiIgnore
  @GetMapping(value = "/elide/docs", produces = {APPLICATION_JSON_VALUE})
  public ResponseEntity<Json> getDocumentation() {
    Info info = new Info().title("Elide JSON API").version("");
    SwaggerBuilder builder = new SwaggerBuilder(entityDictionary, info);
    Swagger document = builder.build()
      .basePath(DataController.PATH_PREFIX);

    return ResponseEntity.ok(jsonSerializer.toJson(document));
  }
}
