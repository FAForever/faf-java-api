package com.faforever.api.coturn;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.data.domain.CoturnServer;
import com.faforever.api.security.FafAuthenticationToken;
import com.faforever.api.security.UserSupplier;
import jakarta.inject.Inject;
import jakarta.persistence.PostLoad;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

@Component
public class CoturnServerEnricher {

  private FafApiProperties fafApiProperties;
  private UserSupplier userSupplier;

  @Inject
  public void init(FafApiProperties fafApiProperties, UserSupplier userSupplier) {
    this.fafApiProperties = fafApiProperties;
    this.userSupplier = userSupplier;
  }

  @PostLoad
  public void enhance(CoturnServer coturnServer) {
    FafAuthenticationToken fafAuthenticationToken = userSupplier.get();

    // Build hmac verification as described here:
    // https://github.com/coturn/coturn/blob/f67326fe3585eafd664720b43c77e142d9bed73c/README.turnserver#L710
    long timestamp = System.currentTimeMillis() / 1000 + fafApiProperties.getCoturn().getTokenLifetimeSeconds();
    String tokenName = String.format("%d:%d", timestamp, fafAuthenticationToken.getUserId());

    String token = Base64.getEncoder().encodeToString(new HmacUtils(HmacAlgorithms.HMAC_SHA_1, coturnServer.getKey()).hmac(tokenName));

    String host = coturnServer.getHost();
    if (coturnServer.getPort() != null) {
      host += ":" + coturnServer.getPort();
    }

    Set<URI> urls = new HashSet<>();
    urls.add(URI.create("turn://%s?transport=tcp".formatted(host)));
    urls.add(URI.create("turn://%s?transport=udp".formatted(host)));
    urls.add(URI.create("turn://%s".formatted(host)));

    coturnServer.setUrls(urls);
    coturnServer.setCredentialType("token");
    coturnServer.setCredential(token);
    coturnServer.setUsername(tokenName);
  }
}
