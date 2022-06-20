package com.faforever.api.data.domain;

import com.faforever.api.data.checks.Prefab;
import com.faforever.api.security.elide.permission.LobbyCheck;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.ReadPermission;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "coturn_servers")
@Include(name = CoturnServer.TYPE_NAME)
@ReadPermission(expression = LobbyCheck.EXPRESSION)
@UpdatePermission(expression = Prefab.NONE)
@Setter
public class CoturnServer {
  public static final String TYPE_NAME = "coturnServer";

  private Integer id;
  private String region;
  private String host;
  private Integer port;
  private String key;
  private boolean active;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  public Integer getId() {
    return id;
  }

  @Column(name = "region")
  public String getRegion() {
    return region;
  }

  @Column(name = "host")
  public String getHost() {
    return host;
  }

  @Column(name = "port")
  public Integer getPort() {
    return port;
  }

  @Column(name = "preshared_key")
  public String getKey() {
    return key;
  }

  @Column(name = "active")
  public boolean isActive() {
    return active;
  }
}
