package com.faforever.api.data.listeners;


import com.faforever.api.config.FafApiProperties;
import com.faforever.api.data.domain.Avatar;
import com.faforever.api.error.ApiException;
import com.faforever.api.error.Error;
import com.faforever.api.error.ErrorCode;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.persistence.PreRemove;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class AvatarListener {
  private static FafApiProperties apiProperties;

  @Inject
  public void init(FafApiProperties apiProperties) {
    AvatarListener.apiProperties = apiProperties;
  }

  @PreRemove
  public void deleteImageFile(Avatar avatar) throws IOException {
    if (!avatar.getAssignments().isEmpty()) {
      throw new ApiException(new Error(ErrorCode.AVATAR_IN_USE));
    }
    final String downloadUrlBase = apiProperties.getAvatar().getDownloadUrlBase();
    final String fileName = avatar.getUrl().replace(downloadUrlBase, "");
    final Path avatarImageFilePath = apiProperties.getAvatar().getTargetDirectory().resolve(fileName);
    Files.deleteIfExists(avatarImageFilePath);
  }
}
