package com.faforever.api.security.jwt;

import com.faforever.api.config.FafApiProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.core.Authentication;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.Date;

//@Service
public class TokenAuthenticationService {

  private final String secret;
  private long EXPIRATION_TIME = Duration.ofDays(10).toMillis();
  private String headerString = "Authorization";

  public TokenAuthenticationService(FafApiProperties properties) {
    this.secret = properties.getJwtSecret();
  }

  void addAuthentication(HttpServletResponse response, String username) {
    String jwt = Jwts.builder()
        .setSubject(username)
        .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
        .signWith(SignatureAlgorithm.HS512, secret)
        .compact();
    response.addHeader(headerString, "Bearer " + jwt);
  }

  Authentication getAuthentication(HttpServletRequest request) {
    String token = request.getHeader(headerString);
    if (token != null) {
      String username = Jwts.parser()
          .setSigningKey(secret)
          .parseClaimsJws(token)
          .getBody()
          .getSubject();
      if (username != null) {
        return new AuthenticatedUser(username);
      }
    }
    return null;
  }
}
