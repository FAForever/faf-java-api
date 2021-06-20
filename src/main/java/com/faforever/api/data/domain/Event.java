package com.faforever.api.data.domain;

import com.faforever.api.data.listeners.EventLocalizationListener;
import com.yahoo.elide.annotation.ComputedAttribute;
import com.yahoo.elide.annotation.Exclude;
import com.yahoo.elide.annotation.Include;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "event_definitions")
@Include(name = Event.TYPE_NAME)
@Setter
@EntityListeners(EventLocalizationListener.class)
public class Event {

  public static final String TYPE_NAME = "event";

  private String id;
  private String nameKey;
  private String imageUrl;
  private Type type;

  // Set by EventLocalizationListener
  private String name;

  @Id
  @Column(name = "id")
  public String getId() {
    return id;
  }

  @Column(name = "name_key")
  @Exclude
  public String getNameKey() {
    return nameKey;
  }

  @Transient
  @ComputedAttribute
  public String getName() {
    return name;
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
    NUMERIC, TIME
  }
}
