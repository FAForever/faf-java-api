package com.faforever.api.featuredmods;

import com.faforever.api.config.FafApiProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import javax.persistence.PostLoad;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;

@Component
public class FeaturedModFileEnricher {

  private static final String HMAC_SHA256 = "HmacSHA256";

  private static FafApiProperties fafApiProperties;

  @Inject
  public void init(FafApiProperties fafApiProperties) {
    FeaturedModFileEnricher.fafApiProperties = fafApiProperties;
  }

  @PostLoad
  public void enhance(FeaturedModFile featuredModFile) throws NoSuchAlgorithmException, InvalidKeyException {
    String folder = featuredModFile.getFolderName();
    String urlFormat = fafApiProperties.getFeaturedMod().getFileUrlFormat();
    String secret = fafApiProperties.getFeaturedMod().getCloudflareHmacSecret();
    long timeStamp = Instant.now().getEpochSecond();
    URI featuredModUri = URI.create(String.format(urlFormat, folder, featuredModFile.getOriginalFileName()));

    // Builds hmac token for cloudflare firewall verification as specified at
    // https://support.cloudflare.com/hc/en-us/articles/115001376488-Configuring-Token-Authentication
    Mac mac = Mac.getInstance(HMAC_SHA256);
    mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256));
    byte[] macMessage = (featuredModUri.getPath() + timeStamp).getBytes(StandardCharsets.UTF_8);

    String hmacEncoded = URLEncoder.encode(new String(Base64.getEncoder().encode(mac.doFinal(macMessage)), StandardCharsets.UTF_8), StandardCharsets.UTF_8);
    String parameter = "%d-%s".formatted(timeStamp, hmacEncoded);

    featuredModFile.setUrl(UriComponentsBuilder.fromUri(featuredModUri).queryParam("verify", parameter).build().toString());
  }
}
