package com.faforever.api.security.oauth2;

import com.faforever.api.security.oauth2.domain.OAuthClient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OAuthClientRepository extends JpaRepository<OAuthClient, String> {

}
