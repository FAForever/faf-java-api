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
import software.amazon.awssdk.http.ContentStreamProvider;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.UncheckedIOException;

@Slf4j
@Service
public class DesyncReportService {
  static final String CONTENT_TYPE = "application/zip";
  private static final String REPORT_FILE_NAME_FORMAT = "desync/%d/%d.zip";

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

  /**
   * Object key of a report in the bucket. Deliberately not configurable: the key is not persisted but derived, so
   * changing it would orphan every report that was already uploaded.
   */
  public String getReportFileName(int gameId, int playerId) {
    return REPORT_FILE_NAME_FORMAT.formatted(gameId, playerId);
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

    // the SDK reads the body more than once (checksum, then upload), so every read needs a fresh stream
    ContentStreamProvider streamProvider = () -> {
      try {
        return report.getInputStream();
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    };

    s3Client.putObject(PutObjectRequest.builder()
      .bucket(properties.getBucket())
      .key(key)
      .contentType(CONTENT_TYPE)
      .contentLength(size)
      .build(), RequestBody.fromContentProvider(streamProvider, size, CONTENT_TYPE));

    // only after the file is stored, so the column can be relied on to filter for existing reports
    gameDesyncRepository.markReportUploaded(gameId, playerId);
  }
}
