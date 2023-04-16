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

    List<Resource> values = coturnService.getCoturnServerDetails(fafAuthenticationToken)
      .stream()
      .map(CoturnController::convertToResource)
      .toList();

    return new JsonApiDocument(new Data<>(values));
  }

  @NotNull
  private static Resource convertToResource(CoturnServerDetails coturnServerDetails) {
    return new Resource("coturnServerDetails", String.valueOf(coturnServerDetails.coturnServerId()),
                        Map.of(
                          "urls", coturnServerDetails.urls(),
                          "username", coturnServerDetails.username(),
                          "credential", coturnServerDetails.credential(),
                          "credentialType", coturnServerDetails.credentialType()
                              ), null, null, null);
  }

}
