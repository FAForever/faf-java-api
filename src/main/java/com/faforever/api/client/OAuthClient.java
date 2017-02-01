package com.faforever.api.client;

import lombok.Setter;
import org.springframework.util.Assert;

import javax.persistence.AttributeConverter;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;


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

  @NotNull
  @Column(name = "name")
  public String getName() {
    return name;
  }

  @NotNull
  @Column(name = "client_secret")
  public String getClientSecret() {
    return clientSecret;
  }

  @NotNull
  @Column(name = "client_type")
  public ClientType getClientType() {
    return clientType;
  }

  @NotNull
  @Column(name = "redirect_uris")
  public String getRedirectUris() {
    return redirectUris;
  }

  @NotNull
  @Column(name = "default_redirect_uri")
  public String getDefaultRedirectUri() {
    return defaultRedirectUri;
  }

  @NotNull
  @Column(name = "default_scope")
  public String getDefaultScope() {
    return defaultScope;
  }

  @Column(name = "icon_url")
  public String getIconUrl() {
    return iconUrl;
  }

  @Converter(autoApply = true)
  public static class ClientTypeConverter implements AttributeConverter<ClientType, String> {

    @Override
    public String convertToDatabaseColumn(ClientType attribute) {
      Assert.isTrue(attribute != null, "'attribute' cannot be null");
      return attribute.getString();
    }

    @Override
    public ClientType convertToEntityAttribute(String dbData) {
      Assert.isTrue(dbData != null, "'dbData' cannot be null");
      return ClientType.fromString(dbData);
    }
  }
}
