package com.faforever.api.data.domain;

import lombok.EqualsAndHashCode;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import java.sql.Timestamp;

@Setter
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "review")
@Inheritance(strategy = InheritanceType.JOINED)
public class Review {
  private Integer id;
  private String text;
  private Byte rating;
  private Timestamp createTime;
  private Timestamp updateTime;

  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Integer getId() {
    return id;
  }

  @Column(name = "text")
  public String getText() {
    return text;
  }

  @Column(name = "rating")
  public Byte getRating() {
    return rating;
  }

  @Column(name = "create_time")
  public Timestamp getCreateTime() {
    return createTime;
  }

  @Column(name = "update_time")
  public Timestamp getUpdateTime() {
    return updateTime;
  }
}
