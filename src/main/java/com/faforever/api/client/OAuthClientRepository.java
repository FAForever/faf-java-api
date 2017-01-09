package com.faforever.api.client;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OAuthClientRepository extends JpaRepository<OAuthClient, String> {

}
