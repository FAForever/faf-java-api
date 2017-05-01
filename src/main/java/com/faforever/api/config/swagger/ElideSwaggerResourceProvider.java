package com.faforever.api.config.swagger;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import springfox.documentation.spring.web.DocumentationCache;
import springfox.documentation.swagger.web.InMemorySwaggerResourcesProvider;
import springfox.documentation.swagger.web.SwaggerResource;

import java.util.List;

@Component
@Primary
public class ElideSwaggerResourceProvider extends InMemorySwaggerResourcesProvider {
  public ElideSwaggerResourceProvider(DocumentationCache documentationCache) {
    super(documentationCache);
  }

  @Override
  public List<SwaggerResource> get() {
    final List<SwaggerResource> swaggerResources = super.get();

    SwaggerResource swaggerResource = new SwaggerResource();
    swaggerResource.setName("data");
    swaggerResource.setLocation("/elide/docs");
    swaggerResource.setSwaggerVersion("2.0");
    swaggerResources.add(swaggerResource);
    return swaggerResources;
  }
}
