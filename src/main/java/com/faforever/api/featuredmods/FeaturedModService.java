package com.faforever.api.featuredmods;

import com.faforever.api.data.domain.FeaturedMod;
import org.jetbrains.annotations.Nullable;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeaturedModService {
  public static final String FEATURED_MODS_CACHE_NAME = "featuredMods";
  public static final String FEATURED_MOD_FILES_CACHE_NAME = "featuredModFiles";

  private final FeaturedModRepository featuredModRepository;
  private final FeaturedModFileRepository featuredModFileRepository;

  public FeaturedModService(FeaturedModRepository featuredModRepository, FeaturedModFileRepository featuredModFileRepository) {
    this.featuredModRepository = featuredModRepository;
    this.featuredModFileRepository = featuredModFileRepository;
  }

  @Cacheable(FEATURED_MOD_FILES_CACHE_NAME)
  public List<FeaturedModFile> getFiles(String modName, @Nullable Integer version) {
    return featuredModFileRepository.getFiles(modName, version);
  }

  @Cacheable(FEATURED_MODS_CACHE_NAME)
  public List<FeaturedMod> getFeaturedMods() {
    return featuredModRepository.findAll();
  }
}
