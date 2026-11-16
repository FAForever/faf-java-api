package com.faforever.api.desync;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.config.FafApiProperties.DesyncReport;
import com.faforever.api.error.ApiException;
import com.faforever.api.error.Error;
import com.faforever.api.error.ErrorCode;
import com.faforever.api.game.GamePlayerStatsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
public class DesyncReportService {
  static final String CONTENT_TYPE = "application/zip";
  private static final Duration URL_LIFETIME = Duration.ofMinutes(15);

  private final DesyncReport properties;
  private final GamePlayerStatsRepository gamePlayerStatsRepository;
  private final S3Presigner s3Presigner;

  public DesyncReportService(FafApiProperties properties,
                             GamePlayerStatsRepository gamePlayerStatsRepository,
                             @Qualifier("desyncReportS3Presigner") S3Presigner s3Presigner) {
    this.properties = properties.getDesyncReport();
    this.gamePlayerStatsRepository = gamePlayerStatsRepository;
    this.s3Presigner = s3Presigner;
  }

  static String getObjectKey(int gameId, int playerId) {
    return "desync/%d/%d.zip".formatted(gameId, playerId);
  }

  /**
   * Creates a presigned url that allows the player to upload exactly one desync report of exactly {@code size} bytes.
   * The size is part of the signature, so the object storage rejects uploads with any other size.
   */
  public DesyncReportUploadResponse createUploadUrl(int playerId, int gameId, long size) {
    long maxSize = properties.getMaxUploadSize().toBytes();
    if (size < 1 || size > maxSize) {
      throw new ApiException(new Error(ErrorCode.DESYNC_REPORT_INVALID_SIZE, maxSize));
    }

    if (!gamePlayerStatsRepository.existsByGameIdAndPlayerId(gameId, playerId)) {
      throw new AccessDeniedException("Player %d did not participate in game %d".formatted(playerId, gameId));
    }

    if (properties.isRequireGpgnetReport() && gamePlayerStatsRepository.countGpgnetDesyncReports(gameId, playerId) == 0) {
      throw new AccessDeniedException("No desync was reported for player %d in game %d".formatted(playerId, gameId));
    }

    String key = getObjectKey(gameId, playerId);
    log.info("Player {} requested a desync report upload url for game {} ({} bytes)", playerId, gameId, size);

    PutObjectRequest putObjectRequest = PutObjectRequest.builder()
      .bucket(properties.getBucket())
      .key(key)
      .contentLength(size)
      .contentType(CONTENT_TYPE)
      .build();

    PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(PutObjectPresignRequest.builder()
      .signatureDuration(URL_LIFETIME)
      .putObjectRequest(putObjectRequest)
      .build());

    Map<String, String> headers = new LinkedHashMap<>();
    presigned.signedHeaders().forEach((name, values) -> {
      if (!"host".equalsIgnoreCase(name) && !values.isEmpty()) {
        headers.put(name, values.getFirst());
      }
    });

    return new DesyncReportUploadResponse(presigned.url().toString(), key, headers);
  }
}
