package com.faforever.api.error;

import lombok.Getter;

@Getter
public enum ErrorCode {
  ACHIEVEMENT_NOT_INCREMENTAL(100, "Invalid operation", "Only incremental achievements can be incremented. Achievement ID: {0}."),
  ACHIEVEMENT_NOT_STANDARD(101, "Invalid operation", "Only standard achievements can be unlocked directly. Achievement ID: {0}."),
  UPLOAD_FILE_MISSING(102, "Missing file", "A file has to be provided as parameter 'file'."),
  PARAMETER_MISSING(103, "Missing parameter", "A parameter '{0}' has to be provided."),
  UPLOAD_INVALID_FILE_EXTENSION(104, "Invalid file extension", "File must have the following extension: {0}."),
  MAP_NAME_TOO_LONG(105, "Invalid map name", "The map name must not exceed {0} characters, was: {1}"),
  MAP_NOT_ORIGINAL_AUTHOR(106, "Permission denied", "Only the original author is allowed to upload new versions of map: {0}."),
  MAP_VERSION_EXISTS(107, "Duplicate map version", "Map '{0}' with version '{1}' already exists."),
  MAP_NAME_CONFLICT(108, "Name clash", "Another map with file name '{0}' already exists."),
  MAP_NAME_MISSING(109, "Missing map name", "The scenario file must specify a map name."),
  MAP_DESCRIPTION_MISSING(110, "Missing description", "The scenario file must specify a map description."),
  MAP_FIRST_TEAM_FFA(111, "Invalid team name", "The name of the first team has to be 'FFA'."),
  MAP_TYPE_MISSING(112, "Missing map type", "The scenario file must specify a map type."),
  MAP_SIZE_MISSING(113, "Missing map size", "The scenario file must specify a map size."),
  MAP_VERSION_MISSING(114, "Missing map version", "The scenario file must specify a map version."),
  QUERY_INVALID_SORT_FIELD(115, "Invalid sort field", "Sorting by '{0}' is not supported"),
  QUERY_INVALID_PAGE_SIZE(116, "Invalid page size", "Page size is not valid: {0}"),
  QUERY_INVALID_PAGE_NUMBER(117, "Invalid page number", "Page number is not valid: {0}"),
  MOD_NAME_MISSING(118, "Missing mod name", "The file mod_info.lua must contain a property 'name'."),
  MOD_UID_MISSING(119, "Missing mod UID", "The file mod_info.lua must contain a property 'uid'."),
  MOD_VERSION_MISSING(120, "Missing mod version", "The file mod_info.lua must contain a property 'version'."),
  MOD_DESCRIPTION_MISSING(121, "Missing mod description", "The file mod_info.lua must contain a property 'description'."),
  MOD_UI_ONLY_MISSING(122, "Missing mod type", "The file mod_info.lua must contain a property 'ui_only'."),
  MOD_NAME_TOO_LONG(123, "Invalid mod name", "The mod name must not exceed {0} characters, was: {1}"),
  MOD_NOT_ORIGINAL_AUTHOR(124, "Permission denied", "Only the original author is allowed to upload new versions of mod: {0}."),
  MOD_VERSION_EXISTS(125, "Duplicate mod version", "Mod '{0}' with version '{1}' already exists."),
  MOD_AUTHOR_MISSING(126, "Missing mod author", "The file mod_info.lua must contain a property 'author'."),
  QUERY_INVALID_RATING_TYPE(127, "Invalid rating type", "Rating type is not valid: {0}. Please pick '1v1' or 'global'."),
  LOGIN_DENIED_BANNED(128, "Login denied", "You are currently banned: {0}"),
  MOD_NAME_CONFLICT(129, "Name clash", "Another mod with file name '{0}' already exists."),
  INVALID_EMAIL(130, "Invalid account data", "The entered email-adress is invalid: {0}"),
  INVALID_USERNAME(131, "Invalid account data", "The entered username is invalid: {0}"),
  USERNAME_TAKEN(132, "Invalid account data", "The entered username is already in use: {0}"),
  EMAIL_REGISTERED(133, "Invalid account data", "The entered email address `{0}` already has an associated account."),
  BLACKLISTED_EMAIL(134, "Invalid account data", "The domain of your email is blacklisted: {0}"),
  TOKEN_INVALID(135, "Invalid operation", "The delivered token is invalid."),
  TOKEN_EXPIRED(136, "Invalid operation", "The delivered token has expired."),
  PASSWORD_RESET_FAILED(137, "Password reset failed", "Username and/or email did not match."),
  PASSWORD_CHANGE_FAILED(138, "Password change failed", "Username and/or old password did not match."),
  USERNAME_CHANGE_TOO_EARLY(139, "Username change not allowed", "Only one name change per 30 days is allowed. {0} more days to go."),
  EMAIL_CHANGE_FAILED(140, "Email change failed", "An unknown error happened while updating the database."),
  STEAM_ID_UNCHANGEABLE(141, "Linking to Steam failed", "Your account is already bound to another Steam ID."),
  UNKNOWN_FEATURED_MOD(142, "Unknown featured mod", "There is no featured mod with ID '{}'."),
  MAP_SCENARIO_LUA_MISSING(143, "Invalid Map File", "Zip File does not contain a *_scenario.lua"),
  MAP_MISSING_MAP_FOLDER_INSIDE_ZIP(144, "No folder inside Zip", "Zip file must contain a folder with all map data"),
  MAP_FILE_INSIDE_ZIP_MISSING(145, "File is missing", "Cannot find inside the zip file {0}"),
  MAP_NO_VALID_MAP_NAME(146, "No valid map name", "No valid map name {0}");

  private final int code;
  private final String title;
  private final String detail;

  ErrorCode(int code, String title, String detail) {
    this.code = code;
    this.title = title;
    this.detail = detail;
  }
}
