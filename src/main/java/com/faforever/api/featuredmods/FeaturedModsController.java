package com.faforever.api.featuredmods;

import com.faforever.api.data.domain.FeaturedMod;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.yahoo.elide.jsonapi.models.Data;
import com.yahoo.elide.jsonapi.models.JsonApiDocument;
import com.yahoo.elide.jsonapi.models.Resource;
import io.swagger.annotations.ApiOperation;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/featuredMods")
public class FeaturedModsController {

  private final FeaturedModService featuredModService;

  public FeaturedModsController(FeaturedModService featuredModService) {
    this.featuredModService = featuredModService;
  }

  @Async
  @RequestMapping(path = "/{modId}/files/{version}")
  @ApiOperation("Lists the required files for a specific featured mod version")
  public CompletableFuture<JsonApiDocument> getFiles(@PathVariable("modId") int modId,
                                                     @PathVariable("version") String version,
                                                     @RequestParam(value = "page[number]", required = false) Integer page) {
    Integer innerPage = Optional.ofNullable(page).orElse(0);
    if (innerPage > 1) {
      return CompletableFuture.completedFuture(new JsonApiDocument(new Data<>(Collections.emptyList())));
    }

    ImmutableMap<Integer, FeaturedMod> mods = Maps.uniqueIndex(featuredModService.getFeaturedMods(), FeaturedMod::getId);
    FeaturedMod featuredMod = mods.get(modId);

    Integer innerVersion = "latest".equals(version) ? null : Integer.valueOf(version);

    List<Resource> values = featuredModService.getFiles(featuredMod.getTechnicalName(), innerVersion).stream()
      .map(modFileMapper())
      .collect(Collectors.toList());

    return CompletableFuture.completedFuture(new JsonApiDocument(new Data<>(values)));
  }

  private Function<FeaturedModFile, Resource> modFileMapper() {
    return file -> new Resource("featuredModFile", String.valueOf(file.getId()),
      ImmutableMap.<String, Object>of(
        "group", file.getGroup(),
        "md5", file.getMd5(),
        "name", file.getName(),
        "url", file.getUrl(),
        "version", file.getVersion()
      ), null, null, null);
  }
}
