package com.faforever.api.coturn;

import com.faforever.api.error.ApiException;
import com.faforever.api.error.ErrorCode;
import com.faforever.api.security.FafAuthenticationToken;
import com.faforever.api.security.OAuthScope;
import com.yahoo.elide.jsonapi.models.Data;
import com.yahoo.elide.jsonapi.models.JsonApiDocument;
import com.yahoo.elide.jsonapi.models.Resource;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "/coturnServers")
@RequiredArgsConstructor
public class CoturnController {

  private final CoturnService coturnService;

  @RequestMapping(path = "/details")
  @ApiOperation("Lists the connection details for each coturn server")
  @PreAuthorize("hasScope('" + OAuthScope._LOBBY + "')")
  public JsonApiDocument getFiles(final Authentication authentication) {
    if (!(authentication instanceof FafAuthenticationToken fafAuthenticationToken)) {
      throw ApiException.of(ErrorCode.NOT_FAF_TOKEN);
    }

    List<Resource> values = coturnService.getCoturnServers(fafAuthenticationToken)
      .stream()
      .map(CoturnController::convertToResource)
      .toList();

    return new JsonApiDocument(new Data<>(values));
  }

  @NotNull
  private static Resource convertToResource(CoturnServers coturnServers) {
    return new Resource("coturnServerDetails", String.valueOf(coturnServers.coturnServerId()),
                        Map.of(
                                "urls", coturnServers.urls(),
                                "username", coturnServers.username(),
                                "credential", coturnServers.credential(),
                                "credentialType", coturnServers.credentialType()
                              ), null, null, null);
  }

}
