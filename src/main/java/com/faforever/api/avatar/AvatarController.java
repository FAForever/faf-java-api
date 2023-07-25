package com.faforever.api.avatar;

import com.faforever.api.error.ErrorResponse;
import com.faforever.api.security.OAuthScope;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static com.faforever.api.data.domain.GroupPermission.ROLE_WRITE_AVATAR;

@RestController
@RequestMapping(path = "/avatars")
public class AvatarController {

  private final AvatarService avatarService;

  public AvatarController(AvatarService avatarService) {
    this.avatarService = avatarService;
  }

  @Operation(summary = "Upload avatar", description = """
    Avatar metadata - {
      "name": "String"
    }""")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "201", description = "Success"),
    @ApiResponse(responseCode = "422", description = "Invalid input", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Failure", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))})
  @ResponseStatus(value = HttpStatus.CREATED)
  @RequestMapping(value = "/upload", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @PreAuthorize("hasScope('" + OAuthScope._ADMINISTRATIVE_ACTION + "') and hasAnyRole('" + ROLE_WRITE_AVATAR + "', 'ROLE_ADMINISTRATOR')")
  public void uploadAvatar(
    @Parameter(name = "metadata") @RequestPart("metadata") AvatarMetadata avatarMetaData,
    @Parameter(name = "file") @RequestPart("file") MultipartFile avatarImageFile) throws IOException {
    avatarService.createAvatar(avatarMetaData, avatarImageFile.getOriginalFilename(), avatarImageFile.getInputStream(), avatarImageFile.getSize());
  }

  @Operation(summary = "Update/Reupload avatar", description = "Avatar metadata - " +
    "{" +
    " \"name\": \"String\"" +
    "}")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success"),
    @ApiResponse(responseCode = "422", description = "Invalid input", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Failure", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))})
  @ResponseStatus(value = HttpStatus.OK)
  @RequestMapping(value = "{avatarId}/upload", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @PreAuthorize("hasScope('" + OAuthScope._ADMINISTRATIVE_ACTION + "') and hasAnyRole('" + ROLE_WRITE_AVATAR + "', 'ROLE_ADMINISTRATOR')")
  public void reuploadAvatar(
    @Parameter(name = "avatarId") @PathVariable("avatarId") Integer avatarId,
    @Parameter(name = "metadata") @RequestPart(value = "metadata") AvatarMetadata avatarMetaData,
    @Parameter(name = "file") @RequestPart("file") MultipartFile avatarImageFile) throws IOException {
    avatarService.updateAvatar(avatarId, avatarMetaData, avatarImageFile.getOriginalFilename(), avatarImageFile.getInputStream(), avatarImageFile.getSize());
  }

  @Operation(summary = "Delete avatar")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "204", description = "Success"),
    @ApiResponse(responseCode = "422", description = "Invalid input", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Failure", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))})
  @ResponseStatus(value = HttpStatus.NO_CONTENT)
  @RequestMapping(value = "/{avatarId}", method = RequestMethod.DELETE)
  @PreAuthorize("hasScope('" + OAuthScope._ADMINISTRATIVE_ACTION + "') and hasAnyRole('" + ROLE_WRITE_AVATAR + "', 'ROLE_ADMINISTRATOR')")
  public void deleteAvatar(
    @Parameter(name = "avatarId") @PathVariable("avatarId") Integer avatarId) throws IOException {
    avatarService.deleteAvatar(avatarId);
  }

}
