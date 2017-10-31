package com.faforever.api.data.domain;

import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "messages")
@Setter
public class Message {

  private int id;
  private String key;
  private String language;
  private String region;
  private String value;

  @Id
  @Column(name = "id")
  public int getId() {
    return id;
  }

  @Column(name = "`key`")
  public String getKey() {
    return key;
  }

  @Column(name = "language")
  public String getLanguage() {
    return language;
  }

  @Column(name = "region")
  public String getRegion() {
    return region;
  }

  @Column(name = "value")
  public String getValue() {
    return value;
  }
}
