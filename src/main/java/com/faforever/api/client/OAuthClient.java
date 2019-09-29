package com.faforever.api.client;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;
import org.springframework.util.Assert;

import javax.persistence.AttributeConverter;
import javax.persistence.Column;
import javax.persistence.Converter;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "oauth_clients")
@Getter
@Setter
public class OAuthClient {

  @Id
  @Column(name = "id")
  private String id;

  @NotNull
  @Column(name = "name")
  private String name;

  @NotNull
  @Column(name = "client_secret")
  private String clientSecret;

  @NotNull
  @Column(name = "client_type")
  private ClientType clientType;

  @NotNull
  @Column(name = "redirect_uris")
  private String redirectUris;

  @NotNull
  @Column(name = "default_redirect_uri")
  private String defaultRedirectUri;

  @NotNull
  @Column(name = "default_scope")
  private String defaultScope;

  @Column(name = "icon_url")
  private String iconUrl;

  @Getter(AccessLevel.NONE) // for type Boolean Lombok generates get* method, not is*
  @Column(name = "auto_approve_scopes")
  private Boolean autoApproveScopes;

  @Nullable
  public Boolean isAutoApproveScopes() {
    return autoApproveScopes;
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
