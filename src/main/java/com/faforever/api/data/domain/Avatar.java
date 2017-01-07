package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import lombok.Data;

import javax.persistence.*;

@Include(rootLevel = true, type = "avatar")
@Entity
@Table(name = "avatars_list")
@Data
public class Avatar {


  private int id;
  private String url;
  private String tooltip;

  @Id
  public int getId() { return id; }
}
