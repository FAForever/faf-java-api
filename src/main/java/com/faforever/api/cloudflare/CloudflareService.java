package com.faforever.api.cloudflare;

import com.faforever.api.config.FafApiProperties;
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
public class CloudflareService {

  private static final String HMAC_SHA256 = "HmacSHA256";

  private final Mac mac = Mac.getInstance(HMAC_SHA256);

  public CloudflareService(FafApiProperties fafApiProperties) throws NoSuchAlgorithmException, InvalidKeyException {
    String secret = fafApiProperties.getCloudflare().getHmacSecret();
    mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256));
  }

  /**
   * Builds hmac token for cloudflare firewall verification as specified
   * <a href="https://support.cloudflare.com/hc/en-us/articles/115001376488-Configuring-Token-Authentication">here</a>
   * @param uri uri to generate the hmac token for
   * @return string representing the hmac token formatted as {timestamp}-{hashedContent}
   */
  public String generateCloudFlareHmacToken(String uri) {
    return generateCloudFlareHmacToken(URI.create(uri));
  }

  /**
   * Builds hmac token for cloudflare firewall verification as specified
   * <a href="https://support.cloudflare.com/hc/en-us/articles/115001376488-Configuring-Token-Authentication">here</a>
   * @param uri uri to generate the hmac token for
   * @return string representing the hmac token formatted as {timestamp}-{hashedContent}
   */
  public String generateCloudFlareHmacToken(URI uri) {
    long timeStamp = Instant.now().getEpochSecond();

    byte[] macMessage = (uri.getPath() + timeStamp).getBytes(StandardCharsets.UTF_8);

    String hmacEncoded = URLEncoder.encode(new String(Base64.getEncoder().encode(mac.doFinal(macMessage)), StandardCharsets.UTF_8), StandardCharsets.UTF_8);
    return "%d-%s".formatted(timeStamp, hmacEncoded);
  }
}
