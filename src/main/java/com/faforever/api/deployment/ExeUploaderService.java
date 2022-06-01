package com.faforever.api.deployment;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.content.ContentService;
import com.faforever.api.error.ApiException;
import com.faforever.api.error.Error;
import com.faforever.api.error.ErrorCode;
import com.faforever.api.featuredmods.FeaturedModFile;
import com.faforever.api.featuredmods.FeaturedModService;
import com.faforever.api.utils.FilePermissionUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import static com.google.common.hash.Hashing.md5;
import static com.google.common.io.Files.asByteSource;
import static java.nio.file.Files.createDirectories;

@Service
@Slf4j
public class ExeUploaderService {
  private final ContentService contentService;
  private final FafApiProperties apiProperties;
  private final FeaturedModService featuredModService;

  public ExeUploaderService(
    ContentService contentService,
    FafApiProperties apiProperties,
    FeaturedModService featuredModService
  ) {
    this.contentService = contentService;
    this.apiProperties = apiProperties;
    this.featuredModService = featuredModService;
  }

  @Transactional
  @SneakyThrows
  public void processUpload(InputStream exeDataInputStream, String modName) {
    checkAllowedBranchName(modName);
    FeaturedModFile featuredModFile = featuredModService.getFile(modName, null, "ForgedAlliance.exe");
    featuredModFile.setName(String.format("ForgedAlliance.%d.exe", featuredModFile.getVersion()));
    Path uploadedFile = this.upload(
      exeDataInputStream,
      featuredModFile.getName(),
      modName
    );
    featuredModFile.setMd5(asByteSource(uploadedFile.toFile()).hash(md5()).toString());
    exeDataInputStream.close();
    List<FeaturedModFile> featuredModFiles = List.of(featuredModFile);
    featuredModService.save(modName, (short) featuredModFile.getVersion(), featuredModFiles);
  }

  private Path upload(InputStream exeDataInputStream, String fileName, String modName) throws IOException {
    Assert.isTrue(exeDataInputStream.available() > 0, "data of 'ForgedAlliance.exe' must not be empty");

    Path tempDir = contentService.createTempDir();
    Path temporaryFile = tempDir.resolve(fileName);
    Files.copy(exeDataInputStream, temporaryFile);

    Path copyTo = getCopyToPath(modName, fileName);
    createDirectories(copyTo.getParent(), FilePermissionUtil.directoryPermissionFileAttributes());
    return Files.copy(
      temporaryFile,
      copyTo,
      StandardCopyOption.REPLACE_EXISTING
    );
  }

  private void checkAllowedBranchName(String modName) throws ApiException {
    if (!"fafbeta".equals(modName) && !"fafdevelop".equals(modName)) {
      throw new ApiException(new Error(ErrorCode.INVALID_FEATURED_MOD, modName));
    }
  }

  private Path getCopyToPath(String modName, String fileName) {
    String copyTo = switch (modName) {
      case "fafbeta" -> apiProperties.getDeployment().getForgedAllianceBetaExePath();
      case "fafdevelop" -> apiProperties.getDeployment().getForgedAllianceDevelopExePath();
      default -> throw new ApiException(new Error(ErrorCode.INVALID_FEATURED_MOD, modName));
    };

    return Paths.get(copyTo, fileName);
  }
}
