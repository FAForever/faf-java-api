package com.faforever.api.deployment;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.config.FafApiProperties.Deployment;
import com.faforever.api.data.domain.FeaturedMod;
import com.faforever.api.deployment.git.GitWrapper;
import com.faforever.api.featuredmods.FeaturedModFile;
import com.faforever.api.featuredmods.FeaturedModService;
import com.faforever.api.utils.FilePermissionUtil;
import com.faforever.commons.fa.ForgedAllianceExePatcher;
import com.faforever.commons.mod.ModReader;
import com.google.common.io.ByteStreams;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import javax.validation.ValidationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.nocatch.NoCatch.noCatch;
import static com.google.common.hash.Hashing.md5;
import static com.google.common.io.Files.hash;
import static java.nio.file.Files.createDirectories;

/**
 * Checks out a specific ref of a featured mod's Git repository and performs the required steps in order to deploy it.
 * At this point, this mechanism is rather ridiculous but that's what legacy products of uneducated people often are. I
 * hope that we'll be able to introduce a sane mechanism that doesn't require a database pretty soon.
 */
@Slf4j
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class LegacyFeaturedModDeploymentTask implements Runnable {

  private static final String NON_WORD_CHARACTER_PATTERN = "[^\\w]";

  private final GitWrapper gitWrapper;
  private final FeaturedModService featuredModService;
  private final FafApiProperties apiProperties;
  private final RestTemplate restTemplate;

  @Setter
  private FeaturedMod featuredMod;

  @Setter
  private Consumer<String> statusDescriptionListener;

  public LegacyFeaturedModDeploymentTask(GitWrapper gitWrapper, FeaturedModService featuredModService, FafApiProperties apiProperties, RestTemplate restTemplate) {
    this.gitWrapper = gitWrapper;
    this.featuredModService = featuredModService;
    this.apiProperties = apiProperties;
    this.restTemplate = restTemplate;
  }

  @Override
  @SneakyThrows
  @Transactional(TxType.MANDATORY)
  public void run() {
    Assert.state(featuredMod != null, "Configuration must be set");
    String modName = featuredMod.getTechnicalName();

    Assert.state(featuredModService.getFeaturedMods().stream()
      .anyMatch(featuredMod -> Objects.equals(featuredMod.getTechnicalName(), modName)), "Unknown mod: " + modName);

    String repositoryUrl = featuredMod.getGitUrl();
    String branch = featuredMod.getGitBranch();
    boolean allowOverride = Optional.ofNullable(featuredMod.isAllowOverride()).orElse(false);
    String modFilesExtension = featuredMod.getFileExtension();
    Map<String, Short> fileIds = featuredModService.getFileIds(modName);

    log.info("Starting deployment of '{}' from '{}', branch '{}', allowOverride '{}', modFilesExtension '{}'",
      modName, repositoryUrl, branch, allowOverride, modFilesExtension);

    Path repositoryDirectory = buildRepositoryDirectoryPath(repositoryUrl);
    checkoutCode(repositoryDirectory, repositoryUrl, branch);

    short version = readModVersion(repositoryDirectory);
    verifyVersion(version, allowOverride, modName);

    Deployment deployment = apiProperties.getDeployment();
    Path targetFolder = Paths.get(deployment.getFeaturedModsTargetDirectory(), String.format(deployment.getFilesDirectoryFormat(), modName));
    List<StagedFile> files = packageFiles(repositoryDirectory, version, fileIds, targetFolder);

    if ("faf".equals(modName)) {
      createPatchedExe(version, fileIds, targetFolder).ifPresent(files::add);
    }

    if (files.isEmpty()) {
      log.warn("Could not find any files to deploy. Is the configuration correct?");
      return;
    }
    files.forEach(this::finalizeFile);

    updateDatabase(files, version, modName);
    invokeDeploymentWebhook(featuredMod);

    log.info("Deployment of '{}' version '{}' was successful", modName, version);
  }

  void invokeDeploymentWebhook(FeaturedMod featuredMod) {
    if (featuredMod.getDeploymentWebhook() == null) {
      log.debug("No deployment webhook configured.");
      return;
    }

    log.debug("Invoking deployment webhook on: {}", featuredMod.getDeploymentWebhook());
    try {
      restTemplate.getForObject(featuredMod.getDeploymentWebhook(), String.class);
    } catch (RestClientException e) {
      log.error("Invoking webhook failed on: {}", featuredMod.getDeploymentWebhook(), e);
    }
  }

  /**
   * Creates a ForgedAlliance.exe which contains the specified version number, if the file is specified for the current
   * featured mod.
   */
  @SneakyThrows
  private Optional<StagedFile> createPatchedExe(short version, Map<String, Short> fileIds, Path targetFolder) {
    String clientFileName = "ForgedAlliance.exe";
    Short fileId = fileIds.get(clientFileName);
    if (fileId == null) {
      log.debug("Skipping '{}' because there's no file ID available", clientFileName);
      return Optional.empty();
    }

    Path targetFile = targetFolder.resolve(String.format("ForgedAlliance.%d.exe", version));
    Path tmpFile = toTmpFile(targetFile);

    createDirectories(tmpFile.getParent(), FilePermissionUtil.directoryPermissionFileAttributes());
    Files.copy(Paths.get(apiProperties.getDeployment().getForgedAllianceExePath()), tmpFile, StandardCopyOption.REPLACE_EXISTING);

    ForgedAllianceExePatcher.patchVersion(tmpFile, version);

    return Optional.of(new StagedFile(fileId, tmpFile, targetFile, clientFileName));
  }

  private short readModVersion(Path modPath) {
    return Short.parseShort(new ModReader().readDirectory(modPath).getVersion().toString());
  }

  private void verifyVersion(int version, boolean allowOverride, String modName) {
    if (!allowOverride) {
      // Normally I'd create a proper query, but this is a hotfix and a protest against the DB-driven patcher
      OptionalInt existingVersion = featuredModService.getFiles(modName, version).stream()
        .mapToInt(FeaturedModFile::getVersion)
        .filter(value -> value == version)
        .findFirst();

      if (existingVersion.isPresent()) {
        throw new ValidationException(String.format("Version '%s' of mod '%s' already exists", version, modName));
      }
    }
  }

  private void updateDatabase(List<StagedFile> files, short version, String modName) {
    updateStatus("Updating database");
    List<FeaturedModFile> featuredModFiles = files.stream()
      .map(file -> new FeaturedModFile()
        .setMd5(noCatch(() -> hash(file.targetFile().toFile(), md5())).toString())
        .setFileId(file.fileId())
        .setName(file.targetFile().getFileName().toString())
        .setVersion(version)
      )
      .toList();

    featuredModService.save(modName, version, featuredModFiles);
  }

  private void updateStatus(String message) {
    Optional.ofNullable(statusDescriptionListener).ifPresent(listener -> listener.accept(message));
  }

  /**
   * Reads all directories (except directories starting with {@code .}), zips their contents and moves the result to the
   * target folder.
   *
   * @return the list of deployed files
   */
  @SneakyThrows
  @SuppressWarnings("unchecked")
  private List<StagedFile> packageFiles(Path repositoryDirectory, short version, Map<String, Short> fileIds, Path targetFolder) {
    updateStatus("Packaging files");
    try (Stream<Path> stream = Files.list(repositoryDirectory)) {
      return stream
        .flatMap(path -> {
          if (Files.isDirectory(path) && !path.getFileName().toString().startsWith(".")) {
            return packDirectory(path, version, targetFolder, fileIds).stream();
          } else if (Files.isRegularFile(path)) {
            return packFile(path, version, targetFolder, fileIds).stream();
          } else{
            return Stream.empty();
          }
        })
        .collect(Collectors.toCollection(ArrayList::new));
    }
  }

  /**
   * Renames the temporary file to the target file so the file is only available under its final name when it is
   * complete, and makes the file readable for everyone.
   */
  private StagedFile finalizeFile(StagedFile file) {
    Path source = file.tmpFile();
    Path target = file.targetFile();

    log.trace("Setting default file permission of '{}'", source);
    FilePermissionUtil.setDefaultFilePermission(source);

    log.trace("Renaming '{}' to '{}'", source, target);
    noCatch(() -> Files.move(source, target, StandardCopyOption.ATOMIC_MOVE));
    return file;
  }

  /**
   * Creates a ZIP file with the file ending configured in {@link #featuredMod}. The content of the ZIP file is the
   * content of the directory. If no file ID is available, an empty optional is returned.
   */
  @SneakyThrows
  private Optional<StagedFile> packDirectory(Path directory, Short version, Path targetFolder, Map<String, Short> fileIds) {
    String directoryName = directory.getFileName().toString();
    Path targetNxtFile = targetFolder.resolve(String.format("%s.%d.%s", directoryName, version, featuredMod.getFileExtension()));
    Path tmpNxtFile = toTmpFile(targetNxtFile);

    // E.g. "effects.nx2"
    String clientFileName = String.format("%s.%s", directoryName, featuredMod.getFileExtension());
    Short fileId = fileIds.get(clientFileName);
    if (fileId == null) {
      log.debug("Skipping folder '{}' because there's no file ID available", directoryName);
      return Optional.empty();
    }

    log.trace("Packaging '{}' to '{}'", directory, targetFolder);

    createDirectories(targetFolder, FilePermissionUtil.directoryPermissionFileAttributes());
    try (ZipArchiveOutputStream outputStream = new ZipArchiveOutputStream(tmpNxtFile.toFile())) {
      zipDirectory(directory, outputStream);
    }
    return Optional.of(new StagedFile(fileId, tmpNxtFile, targetNxtFile, clientFileName));
  }

  /**
   * Creates a ZIP file with the file ending configured in {@link #featuredMod}. The content of the ZIP file is the
   * content of the directory. If no file ID is available, an empty optional is returned.
   */
  @SneakyThrows
  private Optional<StagedFile> packFile(Path file, Short version, Path targetFolder, Map<String, Short> fileIds) {
    String fullFileName = file.getFileName().toString();
    String baseName = FilenameUtils.getBaseName(fullFileName);
    String extension = FilenameUtils.getExtension(fullFileName);
    Path targetFile = targetFolder.resolve(String.format("%s_%d.%s", baseName, version, extension));
    Path tmpFile = toTmpFile(targetFile);

    Short fileId = fileIds.get(fullFileName);
    if (fileId == null) {
      log.debug("Skipping file '{}' because there's no file ID available", fullFileName);
      return Optional.empty();
    }

    log.trace("Copying '{}' to '{}'", file, targetFolder);

    createDirectories(targetFolder, FilePermissionUtil.directoryPermissionFileAttributes());
    Files.copy(file, tmpFile, StandardCopyOption.REPLACE_EXISTING);
    return Optional.of(new StagedFile(fileId, tmpFile, targetFile, fullFileName));
  }

  private void checkoutCode(Path repositoryDirectory, String repoUrl, String branch) throws IOException {
    if (Files.notExists(repositoryDirectory)) {
      createDirectories(repositoryDirectory.getParent(), FilePermissionUtil.directoryPermissionFileAttributes());
      gitWrapper.clone(repoUrl, repositoryDirectory);
    } else {
      gitWrapper.fetch(repositoryDirectory);
    }
    updateStatus("Updating repository");
    gitWrapper.checkoutRef(repositoryDirectory, "refs/remotes/origin/" + branch);
  }

  private Path buildRepositoryDirectoryPath(String repoUrl) {
    String repoDirName = repoUrl.replaceAll(NON_WORD_CHARACTER_PATTERN, "");
    return Paths.get(apiProperties.getDeployment().getRepositoriesDirectory(), repoDirName);
  }

  private Path toTmpFile(Path targetFile) {
    return targetFile.getParent().resolve(targetFile.getFileName().toString() + ".tmp");
  }

  /**
   * Since Java's ZIP implementation uses data descriptors, which FA doesn't implement and therefore cant' read, this
   * implementation uses Apache's commons compress which doesn't use data descriptors as long as the target is a file or
   * a seekable byte channel.
   */
  private void zipDirectory(Path directoryToZip, ZipArchiveOutputStream outputStream) throws IOException {
    Files.walkFileTree(directoryToZip, new SimpleFileVisitor<>() {
      public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        String relativized = directoryToZip.getParent().relativize(dir).toString();
        if (!relativized.isEmpty()) {
          ZipArchiveEntry zipArchiveEntry = new ZipArchiveEntry(relativized + "/");
          zipArchiveEntry.setLastModifiedTime(FileTime.from(Instant.EPOCH));
          outputStream.putArchiveEntry(zipArchiveEntry);
          outputStream.closeArchiveEntry();
        }

        return FileVisitResult.CONTINUE;
      }

      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        log.trace("Zipping file {}", file.toAbsolutePath());
        ZipArchiveEntry zipArchiveEntry = new ZipArchiveEntry(
          file.toFile(),
          directoryToZip.getParent().relativize(file).toString().replace(File.separatorChar, '/'));
        zipArchiveEntry.setLastModifiedTime(FileTime.from(Instant.EPOCH));

        outputStream.putArchiveEntry(zipArchiveEntry);

        try (InputStream inputStream = Files.newInputStream(file)) {
          ByteStreams.copy(inputStream, outputStream);
        }
        outputStream.closeArchiveEntry();
        return FileVisitResult.CONTINUE;
      }
    });
  }

  /**
   * Describes a file that is ready to be deployed. All files should be staged as temporary files first so they can be
   * renamed to their target file name in one go, thus minimizing the time of inconsistent file system state.
   */
  private record StagedFile(
    /**
     * ID of the file as stored in the database.
     */
    short fileId,
    /**
     * The staged file, already in the correct location, that is ready to be renamed.
     */
    Path tmpFile,
    /**
     * The final file name and location.
     */
    Path targetFile,
    /**
     * Name of the file as the client will know it.
     */
    String clientFileName
  ) {
  }
}
