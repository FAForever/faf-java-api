package com.faforever.api.client;

import javax.persistence.AttributeConverter;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Converter;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Objects;


/**
 * Represents an entry in {@code oauth_clients}.
 */
@Entity
@Table(name = "oauth_clients")
public class OAuthClient {

  private String id;
  private String name;
  private String clientSecret;
  private ClientType clientType;
  private String redirectUris;
  private String defaultRedirectUri;
  private String defaultScope;
  private String iconUrl;

  @Id
  @Column(name = "id")
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @Basic
  @Column(name = "name")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Basic
  @Column(name = "client_secret")
  public String getClientSecret() {
    return clientSecret;
  }

  public void setClientSecret(String clientSecret) {
    this.clientSecret = clientSecret;
  }

  @Basic
  @Column(name = "client_type", columnDefinition = "enum('confidential', 'public')")
  public ClientType getClientType() {
    return clientType;
  }

  public void setClientType(ClientType clientType) {
    this.clientType = clientType;
  }

  @Basic
  @Column(name = "redirect_uris")
  public String getRedirectUris() {
    return redirectUris;
  }

  public void setRedirectUris(String redirectUris) {
    this.redirectUris = redirectUris;
  }

  @Basic
  @Column(name = "default_redirect_uri")
  public String getDefaultRedirectUri() {
    return defaultRedirectUri;
  }

  public void setDefaultRedirectUri(String defaultRedirectUri) {
    this.defaultRedirectUri = defaultRedirectUri;
  }

  @Basic
  @Column(name = "default_scope")
  public String getDefaultScope() {
    return defaultScope;
  }

  public void setDefaultScope(String defaultScope) {
    this.defaultScope = defaultScope;
  }

  @Basic
  @Column(name = "icon_url")
  public String getIconUrl() {
    return iconUrl;
  }

  public void setIconUrl(String iconUrl) {
    this.iconUrl = iconUrl;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, clientSecret, clientType, redirectUris, defaultRedirectUri, defaultScope, iconUrl);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OAuthClient that = (OAuthClient) o;
    return Objects.equals(id, that.id) &&
        Objects.equals(name, that.name) &&
        Objects.equals(clientSecret, that.clientSecret) &&
        Objects.equals(clientType, that.clientType) &&
        Objects.equals(redirectUris, that.redirectUris) &&
        Objects.equals(defaultRedirectUri, that.defaultRedirectUri) &&
        Objects.equals(defaultScope, that.defaultScope) &&
        Objects.equals(iconUrl, that.iconUrl);
  }

  @Converter(autoApply = true)
  public static class ClientTypeConverter implements AttributeConverter<ClientType, String> {

    @Override
    public String convertToDatabaseColumn(ClientType attribute) {
      return attribute.getString();
    }

    @Override
    public ClientType convertToEntityAttribute(String dbData) {
      return ClientType.fromString(dbData);
    }
  }
}
