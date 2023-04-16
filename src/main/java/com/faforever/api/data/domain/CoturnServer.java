package com.faforever.api.data.domain;

import com.faforever.api.data.checks.Prefab;
import com.faforever.api.security.elide.permission.LobbyCheck;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.ReadPermission;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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

  // TODO: Remove this once clients migrate to use the coturn controller
  // @ReadPermission(expression = Prefab.NONE)
  @Column(name = "host")
  public String getHost() {
    return host;
  }

  // TODO: Remove this once clients migrate to use the coturn controller
  // @ReadPermission(expression = Prefab.NONE)
  @Column(name = "port")
  public Integer getPort() {
    return port;
  }

  // TODO: Remove this once clients migrate to use the coturn controller
  // @ReadPermission(expression = Prefab.NONE)
  @Column(name = "preshared_key")
  public String getKey() {
    return key;
  }

  @Column(name = "active")
  public boolean isActive() {
    return active;
  }
}
