package com.faforever.api.data.domain;

import com.faforever.api.coturn.CoturnServerEnricher;
import com.faforever.api.data.checks.Prefab;
import com.faforever.api.security.elide.permission.LobbyCheck;
import com.yahoo.elide.annotation.ComputedAttribute;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.ReadPermission;
import com.yahoo.elide.annotation.UpdatePermission;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Setter;

import java.net.URI;
import java.util.Set;

@Entity
@EntityListeners(CoturnServerEnricher.class)
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
  // Enriched by CoturnServerEnricher
  private Set<URI> urls;
  private String username;
  private String credential;
  private String credentialType;

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

  @ReadPermission(expression = Prefab.NONE)
  @Column(name = "host")
  public String getHost() {
    return host;
  }

  @ReadPermission(expression = Prefab.NONE)
  @Column(name = "port")
  public Integer getPort() {
    return port;
  }

  @ReadPermission(expression = Prefab.NONE)
  @Column(name = "preshared_key")
  public String getKey() {
    return key;
  }

  @Column(name = "active")
  public boolean isActive() {
    return active;
  }

  // Enriched by CoturnServerEnricher

  @Transient
  @ComputedAttribute
  public Set<URI> getUrls() {
    return urls;
  }

  @Transient
  @ComputedAttribute
  public String getUsername() {
    return username;
  }

  @Transient
  @ComputedAttribute
  public String getCredential() {
    return credential;
  }

  @Transient
  @ComputedAttribute
  public String getCredentialType() {
    return credentialType;
  }
}
