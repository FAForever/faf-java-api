package com.faforever.api.desync;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.error.ApiException;
import com.faforever.api.error.ErrorCode;
import com.faforever.api.game.GameDesyncRepository;
import com.faforever.api.game.GamePlayerStatsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.util.unit.DataSize;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DesyncReportServiceTest {
  private static final int GAME_ID = 1234;
  private static final int PLAYER_ID = 42;

  @Mock
  private GamePlayerStatsRepository gamePlayerStatsRepository;

  @Mock
  private GameDesyncRepository gameDesyncRepository;

  private FafApiProperties properties;
  private DesyncReportService instance;

  @BeforeEach
  void setUp() {
    properties = new FafApiProperties();
    properties.getDesyncReport().setBucket("desync-reports");
    properties.getDesyncReport().setMaxUploadSize(DataSize.ofMegabytes(10));

    // signing is done locally, so a real presigner with dummy credentials does not need any network access
    S3Presigner presigner = S3Presigner.builder()
      .endpointOverride(URI.create("http://localhost:9000"))
      .region(Region.of("auto"))
      .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("admin", "banana123")))
      .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
      .build();

    instance = new DesyncReportService(properties, gamePlayerStatsRepository, gameDesyncRepository, presigner);
  }

  @Test
  void createUploadUrlSignsKeyAndSize() {
    when(gamePlayerStatsRepository.existsByGameIdAndPlayerId(GAME_ID, PLAYER_ID)).thenReturn(true);

    DesyncReportUploadResponse response = instance.createUploadUrl(PLAYER_ID, GAME_ID, 1_200_000);

    assertThat(response.key(), is("desync/1234/42.zip"));
    assertThat(response.url(), startsWith("http://localhost:9000/desync-reports/desync/1234/42.zip?"));
    // the size and content type are part of the signature, so the storage rejects anything else
    assertThat(response.url(), containsString("content-length"));
    assertThat(response.headers(), hasEntry("content-length", "1200000"));
    assertThat(response.headers(), hasEntry("content-type", "application/zip"));
    assertThat(response.headers().containsKey("host"), is(false));
  }

  @Test
  void createUploadUrlSizeOutOfRange() {
    ApiException tooLarge = assertThrows(ApiException.class,
      () -> instance.createUploadUrl(PLAYER_ID, GAME_ID, DataSize.ofMegabytes(10).toBytes() + 1));
    assertThat(tooLarge.getErrors()[0].getErrorCode(), is(ErrorCode.DESYNC_REPORT_INVALID_SIZE));

    ApiException empty = assertThrows(ApiException.class, () -> instance.createUploadUrl(PLAYER_ID, GAME_ID, 0));
    assertThat(empty.getErrors()[0].getErrorCode(), is(ErrorCode.DESYNC_REPORT_INVALID_SIZE));

    verifyNoInteractions(gamePlayerStatsRepository);
  }

  @Test
  void createUploadUrlAtMaxSize() {
    when(gamePlayerStatsRepository.existsByGameIdAndPlayerId(GAME_ID, PLAYER_ID)).thenReturn(true);

    instance.createUploadUrl(PLAYER_ID, GAME_ID, DataSize.ofMegabytes(10).toBytes());
  }

  @Test
  void createUploadUrlPlayerNotInGame() {
    when(gamePlayerStatsRepository.existsByGameIdAndPlayerId(GAME_ID, PLAYER_ID)).thenReturn(false);

    assertThrows(AccessDeniedException.class, () -> instance.createUploadUrl(PLAYER_ID, GAME_ID, 100));
  }

  @Test
  void gpgnetReportNotCheckedByDefault() {
    when(gamePlayerStatsRepository.existsByGameIdAndPlayerId(GAME_ID, PLAYER_ID)).thenReturn(true);

    instance.createUploadUrl(PLAYER_ID, GAME_ID, 100);

    verifyNoInteractions(gameDesyncRepository);
  }

  @Test
  void gpgnetReportRequiredButMissing() {
    properties.getDesyncReport().setRequireGpgnetReport(true);
    when(gamePlayerStatsRepository.existsByGameIdAndPlayerId(GAME_ID, PLAYER_ID)).thenReturn(true);
    when(gameDesyncRepository.existsByGameIdAndPlayerId(GAME_ID, PLAYER_ID)).thenReturn(false);

    assertThrows(AccessDeniedException.class, () -> instance.createUploadUrl(PLAYER_ID, GAME_ID, 100));
  }

  @Test
  void gpgnetReportRequiredAndPresent() {
    properties.getDesyncReport().setRequireGpgnetReport(true);
    when(gamePlayerStatsRepository.existsByGameIdAndPlayerId(GAME_ID, PLAYER_ID)).thenReturn(true);
    when(gameDesyncRepository.existsByGameIdAndPlayerId(GAME_ID, PLAYER_ID)).thenReturn(true);

    DesyncReportUploadResponse response = instance.createUploadUrl(PLAYER_ID, GAME_ID, 100);

    assertThat(response.key(), is("desync/1234/42.zip"));
  }
}
