/*
 * Copyright 2015, Yahoo Inc.
 * Licensed under the Apache License, Version 2.0
 * See LICENSE file in project root for terms.
 */
package com.faforever.api.data.jackson3compat;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom deserializer for top-level data.
 */
public class DataDeserializer extends ValueDeserializer<Data<Resource>> {

  @Override
  public Data<Resource> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws JacksonException {
    JsonNode node = jsonParser.readValueAsTree();
    if (node.isArray()) {
      List<Resource> resources = new ArrayList<>();
      for (JsonNode n : node) {
        Resource r = deserializationContext.readTreeAsValue(n, Resource.class);
        validateResource(jsonParser, r);
        resources.add(r);
      }
      return new Data<>(resources);
    }
    Resource resource = deserializationContext.readTreeAsValue(node, Resource.class);
    validateResource(jsonParser, resource);
    return new Data<>(resource);
  }

  private void validateResource(JsonParser jsonParser, Resource resource) {
    if (resource.getType() == null || resource.getType().isEmpty()) {
      throw DatabindException.from(jsonParser, "Resource 'type' field is missing or empty.");
    }
  }
}
