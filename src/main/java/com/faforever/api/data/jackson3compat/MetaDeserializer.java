/*
 * Copyright 2019, Yahoo Inc.
 * Licensed under the Apache License, Version 2.0
 * See LICENSE file in project root for terms.
 */
package com.faforever.api.data.jackson3compat;

import com.yahoo.elide.jsonapi.models.Meta;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

import java.util.Map;

/**
 * Custom deserializer for top-level meta object.
 */
public class MetaDeserializer extends ValueDeserializer<Meta> {

  @Override
  public Meta deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws JacksonException {
    JsonNode node = jsonParser.readValueAsTree();
    // Optional top-level meta member must be an object
    return node.isObject() ? new Meta(ctxt.readTreeAsValue(node, Map.class)) : null;
  }
}
