package com.faforever.api.error;

import lombok.Getter;

@Getter
public enum ErrorCode {
  ACHIEVEMENT_NOT_INCREMENTAL(100, "Invalid operation", "Only incremental achievements can be incremented. Achievement ID: {0}."),
  ACHIEVEMENT_NOT_STANDARD(101, "Invalid operation", "Only standard achievements can be unlocked directly. Achievement ID: {0}."),
  UPLOAD_FILE_MISSING(102, "Missing file", "A file has to be provided as parameter 'file'."),
  PARAMETER_MISSING(103, "Missing parameter", "A parameter ''{0}'' has to be provided."),
  UPLOAD_INVALID_FILE_EXTENSIONS(104, "Invalid file extension", "File must have one of the following extensions: {0}."),
  MAP_NAME_TOO_LONG(105, "Invalid map name", "The map name must not exceed {0, number} characters, was: {1, number}"),
  MAP_NOT_ORIGINAL_AUTHOR(106, "Permission denied", "Only the original author is allowed to upload new versions of map: {0}."),
  MAP_VERSION_EXISTS(107, "Duplicate map version", "Map ''{0}'' with version ''{1}'' already exists."),
  MAP_NAME_CONFLICT(108, "Name clash", "Another map with file name ''{0}'' already exists."),
  MAP_NAME_MISSING(109, "Missing map name", "The scenario file must specify a map name."),
  MAP_DESCRIPTION_MISSING(110, "Missing description", "The scenario file must specify a map description."),
  MAP_FIRST_TEAM_FFA(111, "Invalid team name", "The name of the first team has to be 'FFA'."),
  MAP_TYPE_MISSING(112, "Missing map type", "The scenario file must specify a map type."),
  MAP_SIZE_MISSING(113, "Missing map size", "The scenario file must specify a map size."),
  MAP_VERSION_MISSING(114, "Missing map version", "The scenario file must specify a map version."),
  QUERY_INVALID_SORT_FIELD(115, "Invalid sort field", "Sorting by ''{0}'' is not supported"),
  QUERY_INVALID_PAGE_SIZE(116, "Invalid page size", "Page size is not valid: {0, number}"),
  QUERY_INVALID_PAGE_NUMBER(117, "Invalid page number", "Page number is not valid: {0, number}"),
  MOD_NAME_MISSING(118, "Missing mod name", "The file mod_info.lua must contain a property 'name'."),
  MOD_UID_MISSING(119, "Missing mod UID", "The file mod_info.lua must contain a property 'uid'."),
  MOD_VERSION_MISSING(120, "Missing mod version", "The file mod_info.lua must contain a property 'version'."),
  MOD_DESCRIPTION_MISSING(121, "Missing mod description", "The file mod_info.lua must contain a property 'description'."),
  /** @deprecated if it's missing we're just assuming {@code false}, just like {@code selectable}. */
  @Deprecated
  MOD_UI_ONLY_MISSING(122, "Missing mod type", "The file mod_info.lua must contain a property 'ui_only'."),
  MOD_NAME_TOO_LONG(123, "Invalid mod name", "The mod name must not exceed {0, number} characters, was: {1, number}"),
  MOD_NOT_ORIGINAL_AUTHOR(124, "Permission denied", "Only the original author ''{0}'' is allowed to upload new versions of mod ''{1}''."),
  MOD_VERSION_EXISTS(125, "Duplicate mod version", "A mod with name ''{0}'' and version ''{1}'' already exists."),
  MOD_AUTHOR_MISSING(126, "Missing mod author", "The file mod_info.lua must contain a property 'author'."),
  QUERY_INVALID_RATING_TYPE(127, "Invalid rating type", "Rating type is not valid: {0}. Please pick '1v1' or 'global'."),
  LOGIN_DENIED_BANNED(128, "Login denied", "You are currently banned: {0}"),
  MOD_NAME_CONFLICT(129, "Name clash", "Another mod with file name ''{0}'' already exists."),
  EMAIL_INVALID(130, "Invalid account data", "The entered email-address is invalid: {0}"),
  USERNAME_INVALID(131, "Invalid account data", "The entered username is invalid: {0}"),
  USERNAME_TAKEN(132, "Invalid account data", "The entered username is already in use: {0}"),
  EMAIL_REGISTERED(133, "Invalid account data", "The entered email address ''{0}'' already has an associated account."),
  EMAIL_BLACKLISTED(134, "Invalid account data", "The domain of your email is blacklisted: {0}"),
  TOKEN_INVALID(135, "Invalid operation", "The delivered token is invalid."),
  TOKEN_EXPIRED(136, "Invalid operation", "The delivered token has expired."),
  PASSWORD_RESET_FAILED(137, "Password reset failed", "Username and/or email did not match."),
  PASSWORD_CHANGE_FAILED_WRONG_PASSWORD(138, "Password change failed", "Your current password did not match."),
  USERNAME_CHANGE_TOO_EARLY(139, "Username change not allowed", "Only one name change per 30 days is allowed. {0, number} more days to go."),
  EMAIL_CHANGE_FAILED(140, "Email change failed", "An unknown error happened while updating the database."),
  STEAM_ID_UNCHANGEABLE(141, "Linking to Steam failed", "Your Account is already linked to Steam. Just login with our client and start playing."),
  FEATURED_MOD_UNKNOWN(142, "Unknown featured mod", "There is no featured mod with ID ''{0}''."),
  MAP_SCENARIO_LUA_MISSING(143, "Invalid Map File", "Zip file does not contain a *_scenario.lua"),
  MAP_MISSING_MAP_FOLDER_INSIDE_ZIP(144, "No folder inside Zip", "Zip file must contain a folder with all map data"),
  MAP_FILE_INSIDE_ZIP_MISSING(145, "File is missing", "Cannot find needed file with pattern ''{0}'' inside zip file"),
  INVALID_METADATA(146, "Invalid metadata", "Metadata is not valid: {0}"),
  MAP_RENAME_FAILED(147, "Cannot rename to correct name failed ", "Cannot rename file ''{0}''"),
  MAP_INVALID_ZIP(148, "Invalid zip file", "The zip file should only contain one folder at the root level"),
  CLAN_CREATE_FOUNDER_IS_IN_A_CLAN(149, "You are already in a clan", "Clan founder is already member of a clan"),
  CLAN_ACCEPT_TOKEN_EXPIRE(150, "Token Expire", "The Invitation Link expire"),
  CLAN_ACCEPT_WRONG_PLAYER(151, "Wrong Player", "Your are not the invited player"),
  CLAN_ACCEPT_PLAYER_IN_A_CLAN(152, "Player is in a clan", "You are already in a clan"),
  CLAN_NOT_LEADER(153, "You Permission", "You are not the leader of the clan"),
  CLAN_NOT_EXISTS(154, "Cannot find Clan", "Clan with id {0, number} is not available"),
  PLAYER_NOT_FOUND(155, "Player not found", "Cannot find player with id {0, number}."),
  CLAN_NAME_EXISTS(156, "Clan Name already in use", "The clan name ''{0}'' is already in use. Please choose a different clan name."),
  CLAN_TAG_EXISTS(157, "Clan Tag already in use", "The clan tag ''{0}'' is already in use. Please choose a different clan tag."),
  VALIDATION_FAILED(158, "Validation failed", "{0}"),
  MOD_UID_EXISTS(159, "Duplicate mod UID", "A mod with UID ''{0}'' already exists."),
  MOD_STRUCTURE_INVALID(160, "Invalid file structure for mod", "Files in the the root level of the zip file are not allowed. Please ensure all files reside inside a folder."),
  MOD_VERSION_NOT_A_NUMBER(161, "Mod version is not a number", "The mod version has to be a whole number like 123, but was ''{0}''"),
  USERNAME_RESERVED(162, "Username reserved", "The username ''{0}'' is currently reserved for its previous owner. The reservation expires in {1, number} months."),
  UNKNOWN_IDENTIFIER(163, "Unable to resolve user", "The identifier does neither match a username nor an email: {0}"),
  ALREADY_REGISTERED(164, "Registration failed", "You can't create a new account because you already have one."),
  EMAIL_CHANGE_FAILED_WRONG_PASSWORD(138, "Email change failed", "Your current password did not match."),
  STEAM_LINK_NO_FA_GAME(165, "Linking to Steam failed", "You do not own Forged Alliance on Steam or your profile is private. Please set your games list temporarily to 'public'."),
  FILE_NAME_TOO_LONG(166, "Invalid file name", "The file name must not exceed {0, number} character(s), was: {1, number}"),
  FILE_SIZE_EXCEEDED(167, "Invalid file size", "The file size must not exceed {0, number} byte(s), was: {1, number}"),
  AVATAR_NAME_CONFLICT(168, "Invalid avatar file name", "Avatar file name ''{0}'' already exists."),
  AVATAR_IN_USE(169, "Avatar in use", "Could not delete avatar {0, number}. Avatar still in use."),
  ENTITY_NOT_FOUND(170, "Entity not found", "Entity with id: {0} not found."),
  INVALID_AVATAR_DIMENSION(171, "Invalid avatar dimensions", "Avatar dimensions must be {0, number}x{1, number}, was: {2, number}x{3, number}."),
  VOTED_TWICE(172, "You can not vote twice", "There was a vote found for your user and this subject"),
  NOT_ENOUGH_GAMES(173, "You have not got enough games to vote", "You have ''{0}'' games but you need ''{1}''."),
  TOO_MANY_ANSWERS(174, "You have to many answers selected in question", "You selected ''{0}'' but you can only select ''{1}''."),
  VOTE_DID_NOT_START_YET(175, "Vote did not start yet.", "You can not vote if the voting has not started. Vote starts at ''{0}''."),
  VOTE_ALREADY_ENDED(176, "Vote ended  already", "You can not vote if the voting has already finished. Vote ended at ''{0}''."),
  MALFORMATTED_ALTERNATIVE_ORDINALS(177, "You have malformatted alternative ordinals", "Check your preference ordinals. You might have left out an option in between."),
  VOTED_TWICE_ON_ONE_OPTION(178, "Selected one option twice", "You can not vote twice for one option. Voting Choice with id ''{0}'' was selected twice."),
  CAN_NOT_REVEAL_RESULTS_WHEN_VOTING_IS_NOT_FINISHED(179, "Vote still ongoing", "You can reveal results when voting is ongoing. Please set reveal results only after voting finished."),
  VOTING_SUBJECT_DOES_NOT_EXIST(180, "Voting subject does not exist", "There is no voting subject with the ID ''{0}''."),
  VOTING_CHOICE_DOES_NOT_EXIST(181, "Invalid choice", "There is no voting choice with the ID ''{0}''."),
  STEAM_ID_ALREADY_LINKED(182, " Steam account already linked to a FAF account", "You linked this account already to user with name ''{0}''."),
  MAP_NAME_INVALID_CHARACTER(183, "Map name invalid", "Only latin characters, numbers, blanks and the minus are allowed."),
  MOD_NAME_INVALID(184, "Mod name invalid", "The name of the mod in the scenario file can only contain printable ASCII characters and blanks."),
  MAP_NAME_INVALID_MINUS_OCCURENCE(185, "Map name invalid", "Only a maximum of {0} minus characters are allowed."),
  MAP_SCRIPT_LINE_MISSING(186, "Missing scenario.lua line", "The scenario.lua has to contain the following line: {0}"),
  MAP_NAME_TOO_SHORT(187, "Map name invalid", "The map name must have at least {0, number} characters, was: {1, number}"),
  MAP_NAME_DOES_NOT_START_WITH_LETTER(188, "Map name invalid", "The map name has to begin with a letter"),
  PARSING_LUA_FILE_FAILED(189, "Parsing lua files failed", "During the parsing of the lua file an error occured: {0}"),
  NO_RUSH_RADIUS_MISSING(190, "No rush radius missing", "The scenario file must specify a no rush radius"),
  INVALID_FEATURED_MOD(191, "Invalid featured mod name", "The featured mod name ''{0}'' is not allowed in this context."),
  API_KEY_INVALID(192, "Api key is invalid", "The api key is invalid."),
  CLAN_INVALID_FOUNDER(193, "Invalid clan founder", "If you create a clan you must be the founder of it.");

  private final int code;
  private final String title;
  private final String detail;

  ErrorCode(int code, String title, String detail) {
    this.code = code;
    this.title = title;
    this.detail = detail;
  }

  public String codeAsString() {
    return String.valueOf(code);
  }
}
