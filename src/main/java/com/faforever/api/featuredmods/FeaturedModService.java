package com.faforever.api.featuredmods;

import com.faforever.api.data.domain.FeaturedMod;
import org.jetbrains.annotations.Nullable;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class FeaturedModService {
  public static final String FEATURED_MOD_FILES_CACHE_NAME = "FeaturedModService.featuredModFiles";

  private final FeaturedModRepository featuredModRepository;
  private final LegacyFeaturedModFileRepository legacyFeaturedModFileRepository;

  public FeaturedModService(FeaturedModRepository featuredModRepository, LegacyFeaturedModFileRepository legacyFeaturedModFileRepository) {
    this.featuredModRepository = featuredModRepository;
    this.legacyFeaturedModFileRepository = legacyFeaturedModFileRepository;
  }

  public FeaturedModFile getFile(String modName, Integer version, String fileName) {
    return legacyFeaturedModFileRepository.getFile(modName, version, fileName);
  }

  @Cacheable(FEATURED_MOD_FILES_CACHE_NAME)
  public List<FeaturedModFile> getFiles(String modName, @Nullable Integer version) {
    return legacyFeaturedModFileRepository.getFiles(modName, version);
  }

  public List<FeaturedMod> getFeaturedMods() {
    return featuredModRepository.findAll();
  }

  @Transactional
  public void save(String modName, short version, List<FeaturedModFile> featuredModFiles) {
    legacyFeaturedModFileRepository.save(modName, version, featuredModFiles);
  }

  public Map<String, Integer> getFileIds(String modName) {
    return legacyFeaturedModFileRepository.getFileIds(modName);
  }

  public Optional<FeaturedMod> findModById(int id) {
    return featuredModRepository.findById(id);
  }

  public Optional<FeaturedMod> findModByTechnicalName(String name) {
    return featuredModRepository.findOneByTechnicalName(name);
  }

  public Optional<FeaturedMod> findByGitUrlAndGitBranch(String gitHttpTransportUrl, String gitBranch) {
    return featuredModRepository.findByGitUrlAndGitBranch(gitHttpTransportUrl, gitBranch);
  }
}
