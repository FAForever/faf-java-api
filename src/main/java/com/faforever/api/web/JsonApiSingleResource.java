package com.faforever.api.web;

import lombok.Value;

import java.util.function.Supplier;

@Value
public class JsonApiSingleResource<T> {
  JsonApiResourceObject data;

  public static JsonApiSingleResource of(JsonApiResourceObject singleDataObject) {
    return new JsonApiSingleResource(singleDataObject);
  }

  public static <T> JsonApiSingleResource ofProxy(Supplier<String> typeExtractor, Supplier<String> idExtractor,
                                                  Supplier<T> attributesExtractor) {
    return new JsonApiSingleResource<T>(new JsonApiResourceObjectProxy<>(typeExtractor, idExtractor, attributesExtractor));
  }
}
