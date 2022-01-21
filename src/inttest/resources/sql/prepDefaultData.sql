INSERT INTO oauth_clients (id, name, client_secret, client_type, redirect_uris, default_redirect_uri, default_scope)
VALUES ('test', 'test', '{noop}test', 'public', 'http://localhost https://www.getpostman.com/oauth2/callback ',
        'http://localhost',
        'read_events read_achievements upload_map upload_mod upload_avatar write_account_data vote read_sensible_userdata');

INSERT INTO login (id, login, email, password, steamid, ip)
VALUES (1, 'USER', 'user@faforever.com', '92b7b421992ef490f3b75898ec0e511f1a5c02422819d89719b20362b023ee4f', NULL,
        '127.0.0.1'),
       (2, 'MODERATOR', 'moderator@faforever.com', '778ac5b81fa251b450f827846378739caee510c31b01cfa9d31822b88bed8441',
        1234, '127.0.0.1'),
       (3, 'ADMIN', 'admin@faforever.com', '835d6dc88b708bc646d6db82c853ef4182fabbd4a8de59c213f2b5ab3ae7d9be', NULL,
        '127.0.0.1'),
       (4, 'BANNED', 'banned@faforever.com', '', NULL, '127.0.0.1'),
       (5, 'ACTIVE_USER', 'active-user@faforever.com', '', null, '127.0.0.1');

INSERT INTO user_group (id, technical_name, name_key, parent_group_id, public)
VALUES (1, 'ADMINISTRATOR', 'administrator', null, true),
       (2, 'MODERATOR', 'moderator', null, true),
       (3, 'faf_server_administrators', 'user_group.faf.server_administrators', null, true),
       (4, 'faf_moderators_global', 'user_group.faf.moderators.global', null, true),
       (5, 'faf_illuminati', 'user_group.faf.illuminati', null, false);

INSERT INTO user_group (technical_name, name_key, public, parent_group_id)
VALUES ('', 'user_group.faf.server_administrators', 1, @devops_id);

INSERT INTO user_group_assignment (user_id, group_id)
VALUES (2, 2),
       (3, 1);

-- group_permission is populated by R__050_group_permission.sql from Flyway

INSERT INTO group_permission_assignment (group_id, permission_id)
VALUES (1, 1),
       (1, 8),
       (1, 9),
       (1, 10),
       (1, 11),
       (1, 12),
       (1, 13),
       (1, 1),
       (1, 1),
       (1, 1),
       (2, 1),
       (2, 2),
       (2, 3),
       (2, 4),
       (2, 5),
       (2, 6),
       (2, 7),
       (2, 12),
       (2, 19),
       (2, 20),
       (2, 22);

INSERT INTO name_history (change_time, user_id, previous_name)
VALUES (NOW() - INTERVAL 10 YEAR, 3, 'OLD_ADMIN'),
       (NOW(), 2, 'OLD_MODERATOR');

-- leaderboard is populated by R__060_leaderboard.sql from Flyway

-- matchmaker_queue is populated by R__070_matchmaker_queue.sql from Flyway
