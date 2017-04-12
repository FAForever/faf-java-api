package com.faforever.api.deployment;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.config.FafApiProperties.Deployment;
import com.faforever.api.config.FafApiProperties.Deployment.DeploymentConfiguration;
import com.faforever.api.deployment.git.GitWrapper;
import com.faforever.api.featuredmods.FeaturedModFile;
import com.faforever.api.featuredmods.FeaturedModService;
import com.faforever.commons.fa.ForgedAllianceExePatcher;
import com.faforever.commons.mod.ModReader;
import com.faforever.commons.zip.Zipper;
import com.google.common.collect.Sets;
import lombok.Data;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.validation.ValidationException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipOutputStream;

import static com.github.nocatch.NoCatch.noCatch;
import static com.google.common.hash.Hashing.md5;
import static com.google.common.io.Files.hash;
import static java.nio.file.Files.createDirectories;

/**
 * Checks out a specific ref of a featured mod's Git repository and performs the required steps in order to deploy it.
 * At this point, this mechanism is rather ridiculous but that's what legacy products of uneducated people often are.
 * I hope that we'll be able to introduce a sane mechanism that doesn't require a database pretty soon.
 */
@Slf4j
@Component
@Lazy
public class LegacyFeaturedModDeploymentTask implements Runnable {

  private static final String NON_WORD_CHARACTER_PATTERN = "[^\\w]";
  private static final Set<String> VALID_MOD_NAMES = Sets.newHashSet("faf", "fafbeta", "fafdevelop");

  private final GitWrapper gitWrapper;
  private final FeaturedModService featuredModService;
  private final FafApiProperties apiProperties;

  @Setter
  private DeploymentConfiguration configuration;

  public LegacyFeaturedModDeploymentTask(GitWrapper gitWrapper, FeaturedModService featuredModService, FafApiProperties apiProperties) {
    this.gitWrapper = gitWrapper;
    this.featuredModService = featuredModService;
    this.apiProperties = apiProperties;
  }

  @Override
  @SneakyThrows
  public void run() {
    Assert.state(configuration != null, "Configuration must be set");
    String modName = configuration.getModName();
    Assert.state(VALID_MOD_NAMES.contains(modName), "Unsupported mod: " + modName);

    String repositoryUrl = configuration.getRepositoryUrl();
    String branch = configuration.getBranch();
    boolean replaceExisting = configuration.isReplaceExisting();
    String modFilesExtension = configuration.getModFilesExtension();
    Map<String, Short> fileIds = featuredModService.getFileIds(modName);

    log.info("Starting deployment of '{}' from '{}', branch '{}', replaceExisting '{}', modFilesExtension '{}'",
        modName, repositoryUrl, branch, replaceExisting, modFilesExtension);

    Path repositoryDirectory = buildRepositoryDirectoryPath(repositoryUrl);
    checkoutCode(repositoryDirectory, repositoryUrl, branch);

    short version = readModVersion(repositoryDirectory);
    verifyVersion(version, replaceExisting, modName);

    Deployment deployment = apiProperties.getDeployment();
    Path targetFolder = Paths.get(deployment.getFeaturedModsTargetDirectory(), String.format(deployment.getFilesDirectoryFormat(), modName));
    List<StagedFile> files = packageDirectories(repositoryDirectory, version, fileIds, targetFolder);
    createPatchedExe(version, fileIds, targetFolder).ifPresent(files::add);

    if (files.isEmpty()) {
      log.warn("Could not find any files to deploy. Is the configuration correct?");
      return;
    }
    files.forEach(this::renameToFinalFile);

    updateDatabase(files, version, modName);

    log.info("Deployment of '{}' version '{}' was successful", modName, version);
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
    Files.copy(Paths.get(apiProperties.getDeployment().getForgedAllianceExePath()), tmpFile, StandardCopyOption.REPLACE_EXISTING);

    ForgedAllianceExePatcher.patchVersion(tmpFile, version);

    return Optional.of(new StagedFile(fileId, tmpFile, targetFile, clientFileName));
  }

  private short readModVersion(Path modPath) {
    return (short) Integer.parseInt(new ModReader().readDirectory(modPath).getVersion().toString());
  }

  private void verifyVersion(int version, boolean replaceExisting, String modName) {
    if (!replaceExisting && !featuredModService.getFiles(modName, version).isEmpty()) {
      throw new ValidationException(String.format("Version '%s' of mod '%s' already exists", version, modName));
    }
  }

  private void updateDatabase(List<StagedFile> files, short version, String modName) {
    List<FeaturedModFile> featuredModFiles = files.stream()
        .map(file -> new FeaturedModFile()
            .setMd5(noCatch(() -> hash(file.getTargetFile().toFile(), md5())).toString())
            .setFileId(file.getFileId())
            .setName(file.getTargetFile().getFileName().toString())
            .setVersion(version)
        )
        .collect(Collectors.toList());

    featuredModService.save(modName, version, featuredModFiles);
  }

  /**
   * Reads all directories (except directories starting with {@code .}), zips their contents and moves the result to
   * the target folder.
   *
   * @return the list of deployed files
   */
  @SneakyThrows
  private List<StagedFile> packageDirectories(Path repositoryDirectory, short version, Map<String, Short> fileIds, Path targetFolder) {
    try (Stream<Path> stream = Files.list(repositoryDirectory)) {
      return stream
          .filter((path) -> Files.isDirectory(path) && !path.getFileName().toString().startsWith("."))
          .map(path -> packFile(path, version, targetFolder, fileIds))
          .filter(Optional::isPresent)
          .map(Optional::get)
          .collect(Collectors.toList());
    }
  }

  /**
   * Renames the temporary file to the target file so the file is only available under its final name when it is
   * complete.
   */
  private StagedFile renameToFinalFile(StagedFile file) {
    Path source = file.getTmpFile();
    Path target = file.getTargetFile();
    log.trace("Renaming '{}' to '{}", source, target);
    noCatch(() -> Files.move(source, target, StandardCopyOption.ATOMIC_MOVE));
    return file;
  }

  /**
   * Creates a ZIP file with the file ending configured in {@link #configuration}. The content of the ZIP file is the
   * content of the directory. If no file ID is available, an empty optional is returned.
   */
  @SneakyThrows
  private Optional<StagedFile> packFile(Path folderToBeZipped, Short version, Path targetFolder, Map<String, Short> fileIds) {
    String folderName = folderToBeZipped.getFileName().toString();
    Path targetNxtFile = targetFolder.resolve(String.format("%s.%d.nxt", folderName, version));
    Path tmpNxtFile = toTmpFile(targetNxtFile);

    // E.g. "effects.nx2"
    String clientFileName = String.format("%s.%s", folderName, configuration.getModFilesExtension());
    Short fileId = fileIds.get(clientFileName);
    if (fileId == null) {
      log.debug("Skipping folder '{}' because there's no file ID available", folderName);
      return Optional.empty();
    }

    log.trace("Packaging '{}' to '{}'", folderToBeZipped, targetFolder);

    createDirectories(targetFolder);
    try (ZipOutputStream outputStream = new ZipOutputStream(Files.newOutputStream(tmpNxtFile))) {
      Zipper.contentOf(folderToBeZipped).to(outputStream).zip();
    }
    return Optional.of(new StagedFile(fileId, tmpNxtFile, targetNxtFile, clientFileName));
  }

  private void checkoutCode(Path repositoryDirectory, String repoUrl, String branch) throws IOException {
    if (Files.notExists(repositoryDirectory)) {
      createDirectories(repositoryDirectory.getParent());
      gitWrapper.clone(repoUrl, repositoryDirectory);
    } else {
      gitWrapper.fetch(repositoryDirectory);
    }
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
   * Describes a file that is ready to be deployed. All files should be staged as temporary files first so they can be
   * renamed to their target file name in one go, thus minimizing the time of inconsistent file system state.
   */
  @Data
  private class StagedFile {
    /**
     * ID of the file as stored in the database.
     */
    private final int fileId;
    /**
     * The staged file, already in the correct location, that is ready to be renamed.
     */
    private final Path tmpFile;
    /**
     * The final file name and location.
     */
    private final Path targetFile;
    /**
     * Name of the file as the client will know it.
     */
    private final String clientFileName;
  }
}
