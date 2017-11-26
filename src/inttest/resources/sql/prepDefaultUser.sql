DELETE FROM unique_id_users;
DELETE FROM uniqueid;
DELETE FROM global_rating;
DELETE FROM ladder1v1_rating;
DELETE FROM uniqueid_exempt;
DELETE FROM version_lobby;
DELETE FROM friends_and_foes;
DELETE FROM ladder_map;
DELETE FROM map_version_review;
DELETE FROM map_version;
DELETE FROM `map`;
DELETE FROM mod_version_review;
DELETE FROM mod_version;
DELETE FROM `mod`;
DELETE FROM mod_stats;
DELETE FROM oauth_clients;
DELETE FROM updates_faf;
DELETE FROM updates_faf_files;
DELETE FROM avatars;
DELETE FROM avatars_list;
DELETE FROM ban_revoke;
DELETE FROM ban;
DELETE FROM clan_membership;
DELETE FROM clan;
DELETE FROM game_player_stats;
DELETE FROM game_review;
DELETE FROM game_stats;
DELETE FROM game_featuredMods;
DELETE FROM teamkills;
DELETE FROM ladder_division_score;
DELETE FROM ladder_division;
DELETE FROM lobby_admin;
DELETE FROM user_notes;
DELETE FROM login;

INSERT INTO oauth_clients (id, name, client_secret, client_type, redirect_uris, default_redirect_uri, default_scope)
VALUES
  ('test', 'test', 'test', 'public', 'http://localhost https://www.getpostman.com/oauth2/callback ', 'http://localhost',
   'read_events read_achievements upload_map upload_mod upload_avatar write_account_data');

INSERT INTO login (id, login, email, password, steamid)
VALUES (1, 'USER', 'user@faforever.com', '92b7b421992ef490f3b75898ec0e511f1a5c02422819d89719b20362b023ee4f', NULL),
 (2, 'MODERATOR', 'moderator@faforever.com', '778ac5b81fa251b450f827846378739caee510c31b01cfa9d31822b88bed8441', 1234),
 (3, 'ADMIN', 'admin@faforever.com', '835d6dc88b708bc646d6db82c853ef4182fabbd4a8de59c213f2b5ab3ae7d9be', NULL);

INSERT INTO lobby_admin (user_id, `group`) VALUES
  (2, 1),
  (3, 2);

INSERT INTO name_history (change_time, user_id, previous_name) VALUES
  (NOW(), 2, 'OLD_MODERATOR');
