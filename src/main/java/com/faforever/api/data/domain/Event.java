package com.faforever.api.data.domain;

import com.faforever.api.data.listeners.EventLocalizationListener;
import com.yahoo.elide.annotation.ComputedAttribute;
import com.yahoo.elide.annotation.Exclude;
import com.yahoo.elide.annotation.Include;
import lombok.Data;
import lombok.NoArgsConstructor;

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
@Data
@NoArgsConstructor
@EntityListeners(EventLocalizationListener.class)
public class Event {

  public static final String TYPE_NAME = "event";

  @Id
  @Column(name = "id")
  private String id;

  @Column(name = "name_key")
  @Exclude
  private String nameKey;

  @Column(name = "image_url")
  private String imageUrl;

  @Column(name = "type")
  @Enumerated(EnumType.STRING)
  private Type type;

  // Set by EventLocalizationListener
  @Transient
  @ComputedAttribute
  private String name;

  public enum Type {
    NUMERIC, TIME
  }
}
