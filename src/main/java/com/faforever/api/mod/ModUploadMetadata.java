package com.faforever.api.mod;

import java.util.UUID;

public record ModUploadMetadata(
  UUID requestId,
  Integer licenseId,
  String repositoryUrl
) {
}
