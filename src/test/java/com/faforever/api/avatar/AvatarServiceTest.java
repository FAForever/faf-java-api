package com.faforever.api.avatar;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.data.domain.Avatar;
import com.faforever.api.data.domain.AvatarAssignment;
import com.faforever.api.error.ApiExceptionMatcher;
import com.faforever.api.error.ErrorCode;
import com.faforever.api.error.NotFoundApiException;
import com.faforever.api.utils.NameUtil;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.migrationsupport.rules.ExpectedExceptionSupport;
import org.junit.jupiter.migrationsupport.rules.ExternalResourceSupport;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, ExpectedExceptionSupport.class, ExternalResourceSupport.class})
public class AvatarServiceTest {

  private static final long VALID_FILE_SIZE = 1024;
  private static final long TOO_BIG_FILE_SIZE = 2048;
  private static final String AVATAR_NAME = "Nice Avatar";
  private static final String EXISTING_AVATAR_NAME = "Existing Nice Avatar";
  private static final AvatarMetadata AVATAR_METADATA = new AvatarMetadata().setName(AVATAR_NAME);
  private static final String EXISTING_VALID_AVATAR_FILENAME = "qai.png";
  private static final String VALID_AVATAR_FILENAME = "qai2.png";
  private static final String BIG_AVATAR_FILENAME = "donator.png";
  private static final String LONG_AVATAR_FILENAME = "CachedAvataravatar_FightNight_Champion.png";
  private static final String INVALID_EXTENSION_AVATAR_FILENAME = "supcom.jpg";
  private static final String INVALID_AVATAR_DIMENSIONS_FILENAME = "supcom.png";
  private static final int AVATAR_ID = 1;
  private static final String DOWNLOAD_URL_FORMAT = "http://example/%s";
  private static final String AVATARS_FOLDER = "avatars";
  private static final int EXISTING_AVATAR_ID = 1;
  private static final int NON_EXISTING_AVATAR_ID = 2;
  private static final Optional<Avatar> AVATAR = Optional.of(new Avatar()
    .setUrl(String.format(DOWNLOAD_URL_FORMAT, EXISTING_VALID_AVATAR_FILENAME))
    .setTooltip(EXISTING_AVATAR_NAME)
  );

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private Path avatarsPath;
  private AvatarService avatarService;

  @Mock
  private AvatarRepository avatarRepository;

  @BeforeEach
  public void setUp() throws Exception {

    FafApiProperties properties = new FafApiProperties();
    avatarsPath = temporaryFolder.getRoot().toPath().resolve(AVATARS_FOLDER);
    Files.createDirectories(avatarsPath);
    properties.getAvatar()
      .setTargetDirectory(avatarsPath)
      .setDownloadUrlFormat(DOWNLOAD_URL_FORMAT)
      .setAllowedExtensions(Collections.singleton("png"))
      .setMaxSizeBytes(1536)
      .setMaxNameLength(15);

    avatarService = new AvatarService(avatarRepository, properties);
  }

  @Test
  public void newAvatarUploadWithValidName() throws Exception {
    final String avatarFileName = VALID_AVATAR_FILENAME;

    final URL imageResource = loadResource(avatarFileName);
    try (final InputStream imageInputStream = imageResource.openStream()) {
      String worstCasePrefix = "[./><";
      avatarService.createAvatar(AVATAR_METADATA, worstCasePrefix + avatarFileName, imageInputStream, VALID_FILE_SIZE);
      ArgumentCaptor<Avatar> avatarCaptor = ArgumentCaptor.forClass(Avatar.class);
      verify(avatarRepository, times(1)).save(avatarCaptor.capture());

      final Avatar storedAvatar = avatarCaptor.getValue();
      String expectedFilename = NameUtil.normalizeFileName(worstCasePrefix + avatarFileName);
      assertEquals(expectedFilename, storedAvatar.getFilename());
      assertEquals(AVATAR_NAME, storedAvatar.getTooltip());
      assertThat(avatarsPath.resolve(expectedFilename).toFile().length(), is(imageResource.openConnection().getContentLengthLong()));
    }
  }

  @Test
  public void existingAvatarReuploadWithDifferentValidName() throws Exception {
    final Path avatarFilePath = avatarsPath.resolve(EXISTING_VALID_AVATAR_FILENAME);
    Files.copy(loadResource(BIG_AVATAR_FILENAME).openStream(), avatarFilePath);

    when(avatarRepository.findById(EXISTING_AVATAR_ID)).thenReturn(AVATAR);

    final String avatarFileName = VALID_AVATAR_FILENAME;
    final URL imageResource = loadResource(avatarFileName);
    try (final InputStream imageInputStream = imageResource.openStream()) {
      avatarService.updateAvatar(1, AVATAR_METADATA, "[./><" + avatarFileName, imageInputStream, VALID_FILE_SIZE);
      ArgumentCaptor<Avatar> avatarCaptor = ArgumentCaptor.forClass(Avatar.class);
      verify(avatarRepository, times(1)).save(avatarCaptor.capture());

      final Avatar storedAvatar = avatarCaptor.getValue();
      assertEquals(String.format(DOWNLOAD_URL_FORMAT, EXISTING_VALID_AVATAR_FILENAME), storedAvatar.getUrl());
      assertEquals(AVATAR_NAME, storedAvatar.getTooltip());
      assertThat(avatarsPath.resolve(EXISTING_VALID_AVATAR_FILENAME).toFile().length(), is(imageResource.openConnection().getContentLengthLong()));
    }
  }

  @Test
  public void duplicateAvatarUpload() throws Exception {
    final String avatarFileName = VALID_AVATAR_FILENAME;
    when(avatarRepository.findOneByFilename(String.format(avatarFileName))).thenReturn(Optional.of(new Avatar()));
    try (final InputStream imageInputStream = loadResource(avatarFileName).openStream()) {
      expectedException.expect(ApiExceptionMatcher.hasErrorCode(ErrorCode.AVATAR_NAME_CONFLICT));
      avatarService.createAvatar(AVATAR_METADATA, avatarFileName, imageInputStream, VALID_FILE_SIZE);
    }
  }

  @Test
  public void nonExistingAvatarReupload() throws Exception {
    final String avatarFileName = VALID_AVATAR_FILENAME;
    when(avatarRepository.findById(NON_EXISTING_AVATAR_ID)).thenReturn(Optional.empty());
    try (final InputStream imageInputStream = loadResource(avatarFileName).openStream()) {
      expectedException.expect(ApiExceptionMatcher.hasErrorCode(ErrorCode.ENTITY_NOT_FOUND));
      avatarService.updateAvatar(NON_EXISTING_AVATAR_ID, AVATAR_METADATA, avatarFileName, imageInputStream, VALID_FILE_SIZE);
    }
  }

  @Test
  public void invalidExtensionAvatarUpload() throws Exception {
    final String avatarFileName = INVALID_EXTENSION_AVATAR_FILENAME;
    try (final InputStream imageInputStream = loadResource(avatarFileName).openStream()) {
      expectedException.expect(ApiExceptionMatcher.hasErrorCode(ErrorCode.UPLOAD_INVALID_FILE_EXTENSIONS));
      avatarService.createAvatar(AVATAR_METADATA, avatarFileName, imageInputStream, VALID_FILE_SIZE);
    }
  }

  @Test
  public void invalidExtensionAvatarReupload() throws Exception {
    final String avatarFileName = INVALID_EXTENSION_AVATAR_FILENAME;
    when(avatarRepository.findById(EXISTING_AVATAR_ID)).thenReturn(AVATAR);
    try (final InputStream imageInputStream = loadResource(avatarFileName).openStream()) {
      expectedException.expect(ApiExceptionMatcher.hasErrorCode(ErrorCode.UPLOAD_INVALID_FILE_EXTENSIONS));
      avatarService.updateAvatar(EXISTING_AVATAR_ID, AVATAR_METADATA, avatarFileName, imageInputStream, VALID_FILE_SIZE);
    }
  }

  @Test
  public void bigSizeAvatarUpload() throws Exception {
    final String avatarFileName = BIG_AVATAR_FILENAME;
    try (final InputStream imageInputStream = loadResource(avatarFileName).openStream()) {
      expectedException.expect(ApiExceptionMatcher.hasErrorCode(ErrorCode.FILE_SIZE_EXCEEDED));
      avatarService.createAvatar(AVATAR_METADATA, avatarFileName, imageInputStream, TOO_BIG_FILE_SIZE);
    }
  }

  @Test
  public void bigSizeAvatarReupload() throws Exception {
    final String avatarFileName = BIG_AVATAR_FILENAME;
    when(avatarRepository.findById(EXISTING_AVATAR_ID)).thenReturn(AVATAR);
    try (final InputStream imageInputStream = loadResource(avatarFileName).openStream()) {
      expectedException.expect(ApiExceptionMatcher.hasErrorCode(ErrorCode.FILE_SIZE_EXCEEDED));
      avatarService.updateAvatar(EXISTING_AVATAR_ID, AVATAR_METADATA, avatarFileName, imageInputStream, TOO_BIG_FILE_SIZE);
    }
  }

  @Test
  public void longFileNameAvatarUpload() throws Exception {
    final String avatarFileName = LONG_AVATAR_FILENAME;
    try (final InputStream imageInputStream = loadResource(avatarFileName).openStream()) {
      expectedException.expect(ApiExceptionMatcher.hasErrorCode(ErrorCode.FILE_NAME_TOO_LONG));
      avatarService.createAvatar(AVATAR_METADATA, avatarFileName, imageInputStream, VALID_FILE_SIZE);
    }
  }

  @Test
  public void longFileNameAvatarReupload() throws Exception {
    final String avatarFileName = LONG_AVATAR_FILENAME;
    when(avatarRepository.findById(EXISTING_AVATAR_ID)).thenReturn(AVATAR);
    try (final InputStream imageInputStream = loadResource(avatarFileName).openStream()) {
      expectedException.expect(ApiExceptionMatcher.hasErrorCode(ErrorCode.FILE_NAME_TOO_LONG));
      avatarService.updateAvatar(EXISTING_AVATAR_ID, AVATAR_METADATA, avatarFileName, imageInputStream, VALID_FILE_SIZE);
    }
  }

  @Test
  public void invalidDimensionsAvatarUpload() throws Exception {
    final String avatarFileName = INVALID_AVATAR_DIMENSIONS_FILENAME;
    try (final InputStream imageInputStream = loadResource(avatarFileName).openStream()) {
      expectedException.expect(ApiExceptionMatcher.hasErrorCode(ErrorCode.INVALID_AVATAR_DIMENSION));
      avatarService.createAvatar(AVATAR_METADATA, avatarFileName, imageInputStream, VALID_FILE_SIZE);
    }
  }

  @Test
  public void invalidDimensionsAvatarReupload() throws Exception {
    final String avatarFileName = INVALID_AVATAR_DIMENSIONS_FILENAME;
    when(avatarRepository.findById(EXISTING_AVATAR_ID)).thenReturn(AVATAR);
    try (final InputStream imageInputStream = loadResource(avatarFileName).openStream()) {
      expectedException.expect(ApiExceptionMatcher.hasErrorCode(ErrorCode.INVALID_AVATAR_DIMENSION));
      avatarService.updateAvatar(EXISTING_AVATAR_ID, AVATAR_METADATA, avatarFileName, imageInputStream, VALID_FILE_SIZE);
    }
  }

  @Test
  public void deleteAvatar() throws Exception {
    final Avatar avatarToDelete = new Avatar().setUrl(VALID_AVATAR_FILENAME).setAssignments(Collections.emptyList());
    when(avatarRepository.findById(EXISTING_AVATAR_ID)).thenReturn(AVATAR);
    when(avatarRepository.findById(AVATAR_ID)).thenReturn(Optional.of(avatarToDelete));
    final Path avatarFilePath = temporaryFolder.getRoot().toPath().resolve(AVATARS_FOLDER).resolve(VALID_AVATAR_FILENAME);
    Files.copy(loadResource(VALID_AVATAR_FILENAME).openStream(), avatarFilePath);

    avatarService.deleteAvatar(AVATAR_ID);

    verify(avatarRepository, times(1)).delete(avatarToDelete);
    assertThat(avatarFilePath.toFile().exists(), is(false));
  }

  @Test
  public void deleteNotExistingAvatar() throws Exception {
    when(avatarRepository.findById(AVATAR_ID)).thenReturn(Optional.empty());
    expectedException.expect(NotFoundApiException.class);

    avatarService.deleteAvatar(AVATAR_ID);

    verify(avatarRepository, never()).delete(new Avatar());
  }

  @Test
  public void deleteAvatarWithAssignments() throws Exception {
    when(avatarRepository.findById(AVATAR_ID)).thenReturn(Optional.of(new Avatar().setAssignments(Collections.singletonList(new AvatarAssignment()))));
    expectedException.expect(ApiExceptionMatcher.hasErrorCode(ErrorCode.AVATAR_IN_USE));

    avatarService.deleteAvatar(AVATAR_ID);

    verify(avatarRepository, never()).delete(new Avatar());
  }

  private URL loadResource(String filename) {
    return AvatarServiceTest.class.getResource("/avatars/" + filename);
  }
}
