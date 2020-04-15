SET FOREIGN_KEY_CHECKS = 0;
DELETE
FROM reported_user;
DELETE
FROM ban;
DELETE
FROM moderation_report;
DELETE
FROM teamkills;
DELETE
FROM unique_id_users;
DELETE
FROM uniqueid;
DELETE
FROM global_rating;
DELETE
FROM ladder1v1_rating;
DELETE
FROM uniqueid_exempt;
DELETE
FROM version_lobby;
DELETE FROM friends_and_foes;
DELETE FROM ladder_map;
DELETE FROM tutorial;
DELETE FROM map_version_review;
DELETE FROM map_version_reviews_summary;
DELETE FROM map_version;
DELETE FROM `map`;
DELETE FROM mod_version_review;
DELETE FROM mod_version_reviews_summary;
DELETE FROM mod_version;
DELETE FROM `mod`;
DELETE FROM mod_stats;
DELETE FROM oauth_clients;
DELETE FROM updates_faf;
DELETE FROM updates_faf_files;
DELETE FROM avatars;
DELETE FROM avatars_list;
DELETE FROM ban;
DELETE FROM clan_membership;
DELETE FROM clan;
DELETE FROM game_player_stats;
DELETE FROM game_review;
DELETE FROM game_reviews_summary;
DELETE FROM game_stats;
DELETE FROM game_featuredMods;
DELETE
FROM ladder_division_score;
DELETE
FROM ladder_division;
DELETE
FROM lobby_admin;
DELETE
FROM name_history;
DELETE
FROM group_permission_assignment;
DELETE
FROM group_permission;
DELETE
FROM user_group_assignment;
DELETE
FROM user_group;
DELETE
FROM login;
DELETE
FROM email_domain_blacklist;
SET FOREIGN_KEY_CHECKS = 1;
