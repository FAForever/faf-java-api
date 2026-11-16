package com.faforever.api.desync;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.config.FafApiProperties.DesyncReport;
import com.faforever.api.error.ApiException;
import com.faforever.api.error.Error;
import com.faforever.api.error.ErrorCode;
import com.faforever.api.game.GameDesyncRepository;
import com.faforever.api.game.GamePlayerStatsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Service
public class DesyncReportService {
  static final String CONTENT_TYPE = "application/zip";

  private final DesyncReport properties;
  private final GamePlayerStatsRepository gamePlayerStatsRepository;
  private final GameDesyncRepository gameDesyncRepository;
  private final S3Client s3Client;

  public DesyncReportService(FafApiProperties properties,
                             GamePlayerStatsRepository gamePlayerStatsRepository,
                             GameDesyncRepository gameDesyncRepository,
                             @Qualifier("desyncReportS3Client") S3Client s3Client) {
    this.properties = properties.getDesyncReport();
    this.gamePlayerStatsRepository = gamePlayerStatsRepository;
    this.gameDesyncRepository = gameDesyncRepository;
    this.s3Client = s3Client;
  }

  public String getReportFileName(int gameId, int playerId) {
    return properties.getReportFileNameFormat().formatted(gameId, playerId);
  }

  /**
   * Stores the desync report of a player and marks it as uploaded. A repeated upload for the same game replaces the
   * previous report.
   */
  public void storeReport(int playerId, int gameId, MultipartFile report) throws IOException {
    long maxSize = properties.getMaxUploadSize().toBytes();
    long size = report.getSize();
    if (size < 1 || size > maxSize) {
      throw new ApiException(new Error(ErrorCode.DESYNC_REPORT_INVALID_SIZE, maxSize));
    }

    if (!gamePlayerStatsRepository.existsByGameIdAndPlayerId(gameId, playerId)) {
      throw new AccessDeniedException("Player %d did not participate in game %d".formatted(playerId, gameId));
    }

    String key = getReportFileName(gameId, playerId);
    log.info("Player {} uploads a desync report for game {} ({} bytes)", playerId, gameId, size);

    try (InputStream inputStream = report.getInputStream()) {
      s3Client.putObject(PutObjectRequest.builder()
        .bucket(properties.getBucket())
        .key(key)
        .contentType(CONTENT_TYPE)
        .contentLength(size)
        .build(), RequestBody.fromInputStream(inputStream, size));
    }

    // only after the file is stored, so the column can be relied on to filter for existing reports
    gameDesyncRepository.markReportUploaded(gameId, playerId);
  }
}
