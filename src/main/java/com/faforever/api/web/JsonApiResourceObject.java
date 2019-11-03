package com.faforever.api.web;

public interface JsonApiResourceObject {
  String getType();

  String getId();

  Object getAttributes();
}
