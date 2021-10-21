package com.faforever.api.mod;

import lombok.Data;

@Data
public class LuaContent {

  private String name;
  private String version;
  private String author;
  private String copyright;
  private String desc;
  private String uid;
  private boolean exclusive;
  private boolean uiOnly;
  private String iconPath;

  public String asLuaString() {
    StringBuilder b = new StringBuilder();
    addIfNotNull(b, "name", name, "\"");
    addIfNotNull(b, "version", version, null);
    addIfNotNull(b, "author", author, "\"");
    addIfNotNull(b, "copyright", copyright, "\"");
    addIfNotNull(b, "description", desc, "\"");
    addIfNotNull(b, "uid", uid, "\"");
    addIfNotNull(b, "exclusive", exclusive, null);
    addIfNotNull(b, "ui_only", uiOnly, null);
    addIfNotNull(b, "icon", iconPath, "'");
    return b.toString();
  }

  private void addIfNotNull(StringBuilder b, String key, Object value, String enclosing) {
    if (value != null) {
      b.append(key)
        .append(" = ")
        .append(enclosing != null ? enclosing : "")
        .append(value)
        .append(enclosing != null ? enclosing : "")
        .append("\n");
    }
  }

}
