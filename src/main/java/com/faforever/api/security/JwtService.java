package com.faforever.api.security;

import com.faforever.api.config.FafApiProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaSigner;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;

@Service
public class JwtService {
  private final RsaSigner rsaSigner;
  private final RsaVerifier rsaVerifier;
  private final ObjectMapper objectMapper;

  @Inject
  public JwtService(FafApiProperties fafApiProperties, ObjectMapper objectMapper) throws IOException {
    String secretKey = Files.readString(fafApiProperties.getJwt().getSecretKeyPath());
    String publicKey = Files.readString(fafApiProperties.getJwt().getPublicKeyPath());

    this.rsaSigner = new RsaSigner(secretKey);
    this.rsaVerifier = new RsaVerifier(publicKey);
    this.objectMapper = objectMapper;
  }

  public String sign(Object data) throws IOException {
    return JwtHelper.encode(objectMapper.writeValueAsString(data), this.rsaSigner).getEncoded();
  }

  public Jwt decodeAndVerify(String stringToken) {
    return JwtHelper.decodeAndVerify(stringToken, this.rsaVerifier);
  }
}
