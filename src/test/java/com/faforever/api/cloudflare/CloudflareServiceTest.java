package com.faforever.api.cloudflare;

import com.faforever.api.config.FafApiProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class CloudflareServiceTest {

  private static final String HMAC_SHA256 = "HmacSHA256";

  private FafApiProperties fafApiProperties = new FafApiProperties();
  private CloudflareService instance;

  @BeforeEach
  public void setup() throws Exception {
    String secret = "foo";
    fafApiProperties.getCloudflare().setHmacSecret(secret);

    instance = new CloudflareService(fafApiProperties);
  }

  @Test
  public void hmacTokenGeneration() throws Exception {
    String token = instance.generateCloudFlareHmacToken("http://example.com/bar");

    String[] tokenParts = token.split("-");
    String timeStamp = tokenParts[0];

    Mac mac = Mac.getInstance(HMAC_SHA256);
    mac.init(new SecretKeySpec(fafApiProperties.getCloudflare().getHmacSecret().getBytes(StandardCharsets.UTF_8), HMAC_SHA256));
    byte[] macMessage = ("/bar" + timeStamp).getBytes(StandardCharsets.UTF_8);

    String hmacEncoded = URLEncoder.encode(new String(Base64.getEncoder().encode(mac.doFinal(macMessage)), StandardCharsets.UTF_8), StandardCharsets.UTF_8);
    assertEquals(hmacEncoded, tokenParts[1]);
  }
}
