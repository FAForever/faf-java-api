package com.faforever.api.security;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.error.ApiException;
import com.faforever.api.error.ErrorCode;
import com.faforever.api.security.crypto.CertificateUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.RSAKey;
import org.springframework.stereotype.Service;

import jakarta.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;

@Service
public class JwtService {
  private final RSASSASigner rsaSigner;
  private final RSASSAVerifier rsaVerifier;
  private final ObjectMapper objectMapper;

  @Inject
  public JwtService(FafApiProperties fafApiProperties, ObjectMapper objectMapper) throws Exception {
    String secretKey = Files.readString(fafApiProperties.getJwt().getSecretKeyPath());
    String publicKey = Files.readString(fafApiProperties.getJwt().getPublicKeyPath());

    RSAKey parsedSecretKey = (RSAKey) RSAKey.parseFromPEMEncodedObjects(secretKey);
    RSAPublicKey parsedPublicKey = CertificateUtils.parseSSHPublicKey(publicKey);

    this.rsaSigner = new RSASSASigner(parsedSecretKey);
    this.rsaVerifier = new RSASSAVerifier(parsedPublicKey);
    this.objectMapper = objectMapper;
  }

  public String sign(Object data) throws IOException{
    JWSObject jwsObject = new JWSObject(
      new JWSHeader.Builder(JWSAlgorithm.RS256)
        .type(JOSEObjectType.JWT)
        .build(),
      new Payload(objectMapper.writeValueAsString(data))
    );

    try {
      jwsObject.sign(rsaSigner);
    } catch (JOSEException e) {
      throw new IOException(e);
    }

    return jwsObject.serialize();
  }

  public String decodeAndVerify(String stringToken) throws IOException {
    try {
      JWSObject jwsObject = JWSObject.parse(stringToken);
      if (!jwsObject.verify(rsaVerifier)) {
        throw ApiException.of(ErrorCode.TOKEN_INVALID);
      }
      return jwsObject.getPayload().toString();
    } catch (ParseException | JOSEException e) {
      throw new IOException(e);
    }
  }
}
