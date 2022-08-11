package com.faforever.api.cloudflare;

import com.faforever.api.config.FafApiProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class CloudflareService {

  private static final String HMAC_SHA256 = "HmacSHA256";

  private final FafApiProperties fafApiProperties;

  public String generateCloudFlareHmacToken(String featuredModUriString) throws NoSuchAlgorithmException, InvalidKeyException {
    return generateCloudFlareHmacToken(URI.create(featuredModUriString));
  }

  public String generateCloudFlareHmacToken(URI featuredModUri) throws NoSuchAlgorithmException, InvalidKeyException {
    String secret = fafApiProperties.getCloudflare().getHmacSecret();
    long timeStamp = Instant.now().getEpochSecond();

    // Builds hmac token for cloudflare firewall verification as specified at
    // https://support.cloudflare.com/hc/en-us/articles/115001376488-Configuring-Token-Authentication
    Mac mac = Mac.getInstance(HMAC_SHA256);
    mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256));
    byte[] macMessage = (featuredModUri.getPath() + timeStamp).getBytes(StandardCharsets.UTF_8);

    String hmacEncoded = URLEncoder.encode(new String(Base64.getEncoder().encode(mac.doFinal(macMessage)), StandardCharsets.UTF_8), StandardCharsets.UTF_8);
    return "%d-%s".formatted(timeStamp, hmacEncoded);
  }

}
