package com.faforever.api.desync;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.error.ApiException;
import com.faforever.api.error.ErrorCode;
import com.faforever.api.game.GameDesyncRepository;
import com.faforever.api.game.GamePlayerStatsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.util.unit.DataSize;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
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
  @Mock
  private S3Client s3Client;

  private DesyncReportService instance;

  @BeforeEach
  void setUp() {
    FafApiProperties properties = new FafApiProperties();
    properties.getDesyncReport().setBucket("desync-reports");
    properties.getDesyncReport().setMaxUploadSize(DataSize.ofBytes(1000));

    instance = new DesyncReportService(properties, gamePlayerStatsRepository, gameDesyncRepository, s3Client);
  }

  private static MockMultipartFile report(int size) {
    return new MockMultipartFile("file", "dump.zip", "application/zip", new byte[size]);
  }

  @Test
  void storeReportStoresFileThenMarksUploaded() throws IOException {
    when(gamePlayerStatsRepository.existsByGameIdAndPlayerId(GAME_ID, PLAYER_ID)).thenReturn(true);

    instance.storeReport(PLAYER_ID, GAME_ID, report(500));

    ArgumentCaptor<PutObjectRequest> request = ArgumentCaptor.forClass(PutObjectRequest.class);
    InOrder inOrder = inOrder(s3Client, gameDesyncRepository);
    inOrder.verify(s3Client).putObject(request.capture(), any(RequestBody.class));
    inOrder.verify(gameDesyncRepository).markReportUploaded(GAME_ID, PLAYER_ID);

    assertThat(request.getValue().bucket(), is("desync-reports"));
    assertThat(request.getValue().key(), is("desync/1234/42.zip"));
    assertThat(request.getValue().contentLength(), is(500L));
    assertThat(request.getValue().contentType(), is("application/zip"));
  }

  @Test
  void reportFileNameUsesConfiguredFormat() {
    FafApiProperties properties = new FafApiProperties();
    properties.getDesyncReport().setReportFileNameFormat("dumps/%d-%d.zip");
    DesyncReportService custom = new DesyncReportService(properties, gamePlayerStatsRepository, gameDesyncRepository, s3Client);

    assertThat(custom.getReportFileName(1234, 42), is("dumps/1234-42.zip"));
    assertThat(instance.getReportFileName(1234, 42), is("desync/1234/42.zip"));
  }

  @Test
  void storeReportAtMaxSize() throws IOException {
    when(gamePlayerStatsRepository.existsByGameIdAndPlayerId(GAME_ID, PLAYER_ID)).thenReturn(true);

    instance.storeReport(PLAYER_ID, GAME_ID, report(1000));

    verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    verify(gameDesyncRepository).markReportUploaded(GAME_ID, PLAYER_ID);
  }

  @Test
  void storeReportTooLarge() {
    ApiException exception = assertThrows(ApiException.class, () -> instance.storeReport(PLAYER_ID, GAME_ID, report(1001)));

    assertThat(exception.getErrors()[0].getErrorCode(), is(ErrorCode.DESYNC_REPORT_INVALID_SIZE));
    verifyNoInteractions(gamePlayerStatsRepository, s3Client, gameDesyncRepository);
  }

  @Test
  void storeReportEmpty() {
    ApiException exception = assertThrows(ApiException.class, () -> instance.storeReport(PLAYER_ID, GAME_ID, report(0)));

    assertThat(exception.getErrors()[0].getErrorCode(), is(ErrorCode.DESYNC_REPORT_INVALID_SIZE));
    verifyNoInteractions(gamePlayerStatsRepository, s3Client, gameDesyncRepository);
  }

  @Test
  void storeReportPlayerNotInGame() {
    when(gamePlayerStatsRepository.existsByGameIdAndPlayerId(GAME_ID, PLAYER_ID)).thenReturn(false);

    assertThrows(AccessDeniedException.class, () -> instance.storeReport(PLAYER_ID, GAME_ID, report(500)));

    verifyNoInteractions(s3Client, gameDesyncRepository);
  }

  @Test
  void storeReportNotMarkedWhenStorageFails() {
    when(gamePlayerStatsRepository.existsByGameIdAndPlayerId(GAME_ID, PLAYER_ID)).thenReturn(true);
    when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class))).thenThrow(S3Exception.builder().build());

    assertThrows(S3Exception.class, () -> instance.storeReport(PLAYER_ID, GAME_ID, report(500)));

    verifyNoInteractions(gameDesyncRepository);
  }
}
