package com.faforever.api.coturn;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.data.domain.CoturnServer;
import com.faforever.api.security.FafAuthenticationToken;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CoturnService {

  private final CoturnServerRepository coturnServerRepository;
  private final FafApiProperties fafApiProperties;

  public List<CoturnServerDetails> getCoturnServerDetails(FafAuthenticationToken fafAuthenticationToken) {
    return coturnServerRepository.findAll().stream()
             .map(coturnServer -> getCoturnServerDetails(coturnServer, fafAuthenticationToken))
             .toList();
  }

  private CoturnServerDetails getCoturnServerDetails(CoturnServer coturnServer, FafAuthenticationToken fafAuthenticationToken) {
    // Build hmac verification as described here:
    // https://github.com/coturn/coturn/blob/f67326fe3585eafd664720b43c77e142d9bed73c/README.turnserver#L710
    long timestamp = System.currentTimeMillis() / 1000 + fafApiProperties.getCoturn().getTokenLifetimeSeconds();
    String tokenName = String.format("%d:%d", timestamp, fafAuthenticationToken.getUserId());

    String token = Base64.getEncoder().encodeToString(new HmacUtils(HmacAlgorithms.HMAC_SHA_1, coturnServer.getKey()).hmac(tokenName));

    String host = coturnServer.getHost();
    if (coturnServer.getPort() != null) {
      host += ":" + coturnServer.getPort();
    }

    Set<String> urls = new HashSet<>();
    urls.add("turn:%s?transport=tcp".formatted(host));
    urls.add("turn:%s?transport=udp".formatted(host));
    urls.add("turn:%s".formatted(host));

    return new CoturnServerDetails(coturnServer.getId(), urls, tokenName, token, "token");
  }
}
