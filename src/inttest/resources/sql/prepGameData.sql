DELETE FROM game_stats;
DELETE FROM game_featuredMods;

INSERT INTO game_featuredMods (id, gamemod, name, description, publish, git_url, git_branch, file_extension, allow_override)
VALUES
  (1, 'faf', 'FAF', 'Forged Alliance Forever', 1, 'https://github.com/FAForever/fa.git', 'deploy/faf', 'nx2', FALSE),
  (6, 'ladder1v1', 'FAF', 'Ladder games', 1, 'https://github.com/FAForever/fa.git', 'deploy/faf', 'nx2', TRUE),
  (25, 'coop', 'Coop', 'Multiplayer campaign games', 1, 'https://github.com/FAForever/fa-coop.git', 'master', 'cop',
   TRUE);

-- INSERT INTO `game_validity` (`id`, `message`) VALUES (0, 'Valid') ON DUPLICATE KEY UPDATE `id` = VALUES(id);

INSERT INTO game_stats (id, startTime, gameName, gameType, gameMod, `host`, mapId, validity) VALUES
  (1, NOW(), 'Test game', '0', 6, 1, 1, 0);
