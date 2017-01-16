package com.faforever.api.utils;

import com.google.common.collect.ImmutableMap;
import lombok.Setter;

import java.io.Serializable;
import java.util.Map;

@Setter
public class JsonApiErrorBuilder {

  private String title;
  private String source;
  private String pointer = "";

  public Map<String, Serializable> build() {
    ImmutableMap source = ImmutableMap.of("pointer", pointer);
    ImmutableMap<String, Serializable> error = ImmutableMap.of(
        "title", title,
        "source", source);
    return ImmutableMap.of("errors", new ImmutableMap[]{error});
  }
}
