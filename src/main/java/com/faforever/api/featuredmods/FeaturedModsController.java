package com.faforever.api.featuredmods;

import com.faforever.api.data.domain.FeaturedMod;
import com.faforever.api.error.ApiException;
import com.faforever.api.error.Error;
import com.faforever.api.security.OAuthScope;
import com.yahoo.elide.jsonapi.models.Data;
import com.yahoo.elide.jsonapi.models.JsonApiDocument;
import com.yahoo.elide.jsonapi.models.Resource;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static com.faforever.api.error.ErrorCode.FEATURED_MOD_UNKNOWN;

@RestController
@RequestMapping(path = "/featuredMods")
@RequiredArgsConstructor
public class FeaturedModsController {

  private final FeaturedModService featuredModService;

  @RequestMapping(path = "/{modId}/files/{version}")
  @ApiOperation("Lists the required files for a specific featured mod version")
  @PreAuthorize("hasScope('" + OAuthScope._LOBBY + "')")
  public JsonApiDocument getFiles(@PathVariable("modId") int modId,
                                  @PathVariable("version") String version,
                                  @RequestParam(value = "page[number]", required = false) Integer page) {
    Integer innerPage = Optional.ofNullable(page).orElse(0);
    if (innerPage > 1) {
      return new JsonApiDocument(new Data<>(List.of()));
    }

    FeaturedMod featuredMod = featuredModService.findModById(modId)
                                                .orElseThrow(() -> new ApiException(new Error(FEATURED_MOD_UNKNOWN, modId)));

    Integer innerVersion = "latest".equals(version) ? null : Integer.valueOf(version);

    List<Resource> values = featuredModService.getFiles(featuredMod.getTechnicalName(), innerVersion).stream()
                                              .map(modFileMapper())
                                              .toList();

    return new JsonApiDocument(new Data<>(values));
  }

  private Function<FeaturedModFile, Resource> modFileMapper() {
    return file -> new Resource("featuredModFile", String.valueOf(file.getId()),
      Map.of(
        "group", file.getGroup(),
        "md5", file.getMd5(),
        "name", file.getName(),
        "url", file.getUrl(),
        "cacheableUrl", file.getCacheableUrl(),
        "hmacToken", file.getHmacToken(),
        "hmacParameter", file.getHmacParameter(),
        "version", file.getVersion()
      ), null, null, null);
  }
}
