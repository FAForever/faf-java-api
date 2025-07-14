package com.faforever.api.mod;

import java.util.UUID;

public record UploadUrlResponse(String uploadUrl, UUID requestId) {
}
