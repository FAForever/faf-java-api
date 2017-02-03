package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "event_definitions")
@Include(rootLevel = true, type = "event")
@Setter
public class EventDefinition {

  private String id;
  private String nameKey;
  private String imageUrl;
  private Type type;

  @Id
  @Column(name = "id")
  public String getId() {
    return id;
  }

  @Column(name = "name_key")
  public String getNameKey() {
    return nameKey;
  }

  @Column(name = "image_url")
  public String getImageUrl() {
    return imageUrl;
  }

  @Column(name = "type")
  @Enumerated(EnumType.STRING)
  public Type getType() {
    return type;
  }

  public enum Type {
    NUMERIC, TIME;
  }
}
