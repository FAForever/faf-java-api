package com.faforever.api.client;

import lombok.Setter;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


/**
 * Represents an entry in {@code oauth_clients}.
 */
@Entity
@Table(name = "oauth_clients")
@Setter
public class OAuthClient {

  private String id;
  private String name;
  private String clientSecret;
  private String clientType;
  private String redirectUris;
  private String defaultRedirectUri;
  private String defaultScope;
  private String iconUrl;

  @Id
  @Column(name = "id")
  public String getId() {
    return id;
  }

  @Basic
  @Column(name = "name")
  public String getName() {
    return name;
  }

  @Basic
  @Column(name = "client_secret")
  public String getClientSecret() {
    return clientSecret;
  }

  @Basic
  @Column(name = "client_type")
  public String getClientType() {
    return clientType;
  }

  @Basic
  @Column(name = "redirect_uris")
  public String getRedirectUris() {
    return redirectUris;
  }

  @Basic
  @Column(name = "default_redirect_uri")
  public String getDefaultRedirectUri() {
    return defaultRedirectUri;
  }

  @Basic
  @Column(name = "default_scope")
  public String getDefaultScope() {
    return defaultScope;
  }

  @Basic
  @Column(name = "icon_url")
  public String getIconUrl() {
    return iconUrl;
  }

}
