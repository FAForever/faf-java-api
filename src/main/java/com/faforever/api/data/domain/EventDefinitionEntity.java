package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "event_definitions")
@Include(rootLevel = true, type = "event_definition")
public class EventDefinitionEntity {

  public enum Type {
    NUMERIC, TIME;
  }

  private String id;
  private String nameKey;
  private String imageUrl;
  private Type type;

  @Id
  @Column(name = "id")
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @Basic
  @Column(name = "name_key")
  public String getNameKey() {
    return nameKey;
  }

  public void setNameKey(String nameKey) {
    this.nameKey = nameKey;
  }

  @Basic
  @Column(name = "image_url")
  public String getImageUrl() {
    return imageUrl;
  }

  public void setImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
  }

  @Basic
  @Column(name = "type")
  @Enumerated(EnumType.STRING)
  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, nameKey, imageUrl, type);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EventDefinitionEntity that = (EventDefinitionEntity) o;
    return Objects.equals(id, that.id) &&
        Objects.equals(nameKey, that.nameKey) &&
        Objects.equals(imageUrl, that.imageUrl) &&
        Objects.equals(type, that.type);
  }
}
