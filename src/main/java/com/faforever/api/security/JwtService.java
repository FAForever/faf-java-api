package com.faforever.api.security;

import com.faforever.api.config.FafApiProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.MacSigner;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

@Service
public class JwtService {
  private final MacSigner macSigner;
  private final ObjectMapper objectMapper;

  @Inject
  public JwtService(FafApiProperties fafApiProperties, ObjectMapper objectMapper) {
    this.macSigner = new MacSigner(fafApiProperties.getJwtSecret());
    this.objectMapper = objectMapper;
  }

  public String sign(Map<String, Serializable> data) throws IOException {
    Jwt token = JwtHelper.encode(objectMapper.writeValueAsString(data), this.macSigner);
    return token.getEncoded();
  }

  public Jwt decodeAndVerify(String stringToken) {
    return JwtHelper.decodeAndVerify(stringToken, this.macSigner);
  }
}
