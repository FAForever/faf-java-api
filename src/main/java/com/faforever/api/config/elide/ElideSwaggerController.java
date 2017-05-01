package com.faforever.api.config.elide;

import com.yahoo.elide.contrib.swagger.SwaggerBuilder;
import com.yahoo.elide.core.EntityDictionary;
import io.swagger.models.Info;
import io.swagger.models.Swagger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import springfox.documentation.annotations.ApiIgnore;
import springfox.documentation.spring.web.json.Json;
import springfox.documentation.spring.web.json.JsonSerializer;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Controller
public class ElideSwaggerController {

  @Autowired
  private JsonSerializer jsonSerializer;
  @Autowired
  private EntityDictionary entityDictionary;

  @ApiIgnore
  @RequestMapping(value = "/elide/docs",
    method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE})
  public
  @ResponseBody
  ResponseEntity<Json> getDocumentation() {
    Info info = new Info().title("Elide JSON API").version("1.0");
    SwaggerBuilder builder = new SwaggerBuilder(entityDictionary, info);
    Swagger document = builder.build();

    return new ResponseEntity<Json>(jsonSerializer.toJson(document), HttpStatus.OK);
  }

}
