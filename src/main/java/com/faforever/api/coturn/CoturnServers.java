package com.faforever.api.coturn;

import java.util.Set;

public record CoturnServers(int coturnServerId,
                            Set<String> urls,
                            String username,
                            String credential,
                            String credentialType) {
  public CoturnServers(int coturnServerId, Set<String> urls, String username, String credential, String credentialType) {
    this.coturnServerId = coturnServerId;
    this.urls = Set.copyOf(urls);
    this.username = username;
    this.credential = credential;
    this.credentialType = credentialType;
  }
}
