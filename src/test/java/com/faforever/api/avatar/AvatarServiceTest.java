package com.faforever.api.avatar;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.data.domain.Avatar;
import com.faforever.api.data.domain.AvatarAssignment;
import com.faforever.api.error.ApiExceptionWithCode;
import com.faforever.api.error.ErrorCode;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.persistence.EntityNotFoundException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AvatarServiceTest {

  private static final long VALID_FILE_SIZE = 1024;
  private static final long TOO_BIG_FILE_SIZE = 2048;
  private static final String TOOLTIP = "Very nice avatar";
  private static final String VALID_AVATAR_FILENAME = "qai2.png";
  private static final String BIG_AVATAR_FILENAME = "donator.png";
  private static final String LONG_AVATAR_FILENAME = "CachedAvataravatar_FightNight_Champion.png";
  private static final String INVALID_EXTENSION_AVATAR_FILENAME = "supcom.jpg";
  protected static final int AVATAR_ID = 1;

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private AvatarService avatarService;

  @Mock
  private AvatarRepository avatarRepository;

  @Before
  public void setUp() throws Exception {

    FafApiProperties properties = new FafApiProperties();
    final Path avatarsPath = temporaryFolder.getRoot().toPath().resolve("avatars");
    Files.createDirectories(avatarsPath);
    properties.getAvatar()
      .setTargetDirectory(avatarsPath)
      .setDownloadUrlBase("")
      .setAllowedFileExtensions(Collections.singletonList("png"))
      .setMaxSizeBytes(1536)
      .setMaxNameLength(15);

    avatarService = new AvatarService(avatarRepository, properties);

    when(avatarRepository.findByUrl(any())).thenReturn(Optional.empty());
  }

  @Test
  public void newAvatarUpload() throws Exception {
    final String avatarFileName = VALID_AVATAR_FILENAME;
    try (final InputStream imageInputStream = loadResourceAsStream(avatarFileName)) {
      avatarService.processUploadedAvatar(TOOLTIP, avatarFileName, imageInputStream, VALID_FILE_SIZE);
      ArgumentCaptor<Avatar> avatarCaptor = ArgumentCaptor.forClass(Avatar.class);
      verify(avatarRepository, times(1)).save(avatarCaptor.capture());

      final Avatar storedAvatar = avatarCaptor.getValue();
      assertEquals(avatarFileName, storedAvatar.getUrl());
      assertEquals(TOOLTIP, storedAvatar.getTooltip());
    }
  }

  @Test
  public void duplicateAvatarUpload() throws Exception {
    final String avatarFileName = VALID_AVATAR_FILENAME;
    when(avatarRepository.findByUrl(avatarFileName)).thenReturn(Optional.of(new Avatar()));
    try (final InputStream imageInputStream = loadResourceAsStream(avatarFileName)) {
      expectedException.expect(ApiExceptionWithCode.apiExceptionWithCode(ErrorCode.AVATAR_NAME_CONFLICT));
      avatarService.processUploadedAvatar(TOOLTIP, avatarFileName, imageInputStream, VALID_FILE_SIZE);
    }
  }

  @Test
  public void invalidExtensionAvatarUpload() throws Exception {
    final String avatarFileName = INVALID_EXTENSION_AVATAR_FILENAME;
    try (final InputStream imageInputStream = loadResourceAsStream(avatarFileName)) {
      expectedException.expect(ApiExceptionWithCode.apiExceptionWithCode(ErrorCode.UPLOAD_INVALID_FILE_EXTENSIONS));
      avatarService.processUploadedAvatar(TOOLTIP, avatarFileName, imageInputStream, VALID_FILE_SIZE);
    }
  }

  @Test
  public void bigSizeAvatarUpload() throws Exception {
    final String avatarFileName = BIG_AVATAR_FILENAME;
    try (final InputStream imageInputStream = loadResourceAsStream(avatarFileName)) {
      expectedException.expect(ApiExceptionWithCode.apiExceptionWithCode(ErrorCode.FILE_SIZE_EXCEEDED));
      avatarService.processUploadedAvatar(TOOLTIP, avatarFileName, imageInputStream, TOO_BIG_FILE_SIZE);
    }
  }

  @Test
  public void longFileNameAvatarUpload() throws Exception {
    final String avatarFileName = LONG_AVATAR_FILENAME;
    try (final InputStream imageInputStream = loadResourceAsStream(avatarFileName)) {
      expectedException.expect(ApiExceptionWithCode.apiExceptionWithCode(ErrorCode.FILE_NAME_TOO_LONG));
      avatarService.processUploadedAvatar(TOOLTIP, avatarFileName, imageInputStream, VALID_FILE_SIZE);
    }
  }

  @Test
  public void deleteAvatar() throws Exception {
    final Avatar avatarToDelete = new Avatar().setUrl(VALID_AVATAR_FILENAME).setAssignments(Collections.emptyList());
    when(avatarRepository.getOne(AVATAR_ID)).thenReturn(avatarToDelete);
    Files.copy(loadResourceAsStream(VALID_AVATAR_FILENAME), temporaryFolder.getRoot().toPath().resolve("avatars").resolve(VALID_AVATAR_FILENAME));
    avatarService.deleteAvatar(AVATAR_ID);
    verify(avatarRepository, times(1)).delete(avatarToDelete);
  }

  @Test
  public void deleteNotExistingAvatar() throws Exception {
    when(avatarRepository.getOne(AVATAR_ID)).thenThrow(EntityNotFoundException.class);
    expectedException.expect(EntityNotFoundException.class);
    avatarService.deleteAvatar(AVATAR_ID);
    verify(avatarRepository, never()).delete(new Avatar());
  }

  @Test
  public void deleteAvatarWithAssignments() throws Exception {
    when(avatarRepository.getOne(AVATAR_ID)).thenReturn(new Avatar().setAssignments(Collections.singletonList(new AvatarAssignment())));
    expectedException.expect(ApiExceptionWithCode.apiExceptionWithCode(ErrorCode.AVATAR_IN_USE));
    avatarService.deleteAvatar(AVATAR_ID);
    verify(avatarRepository, never()).delete(new Avatar());
  }

  private InputStream loadResourceAsStream(String filename) {
    return AvatarServiceTest.class.getResourceAsStream("/avatars/" + filename);
  }
}
