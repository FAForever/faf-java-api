package com.faforever.api.web;

import java.util.function.Supplier;

class JsonApiResourceObjectProxy<T> implements JsonApiResourceObject {
  private final Supplier<String> typeExtractor;
  private final Supplier<String> idExtractor;
  private final Supplier<T> attributesExtractor;

  JsonApiResourceObjectProxy(Supplier<String> typeExtractor, Supplier<String> idExtractor, Supplier<T> attributesExtractor) {
    this.typeExtractor = typeExtractor;
    this.idExtractor = idExtractor;
    this.attributesExtractor = attributesExtractor;
  }

  public String getType() {
    return typeExtractor.get();
  }

  public String getId() {
    return idExtractor.get();
  }

  public T getAttributes() {
    return attributesExtractor.get();
  }
}
