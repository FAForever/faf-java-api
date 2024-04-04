SET FOREIGN_KEY_CHECKS = 0;

INSERT INTO game_stats (id, startTime, gameName, gameType, gameMod, `host`, mapId, validity) VALUES
  (1, NOW(), 'Test game', 'DEMORALIZATION', 6, 1, 1, 0);


SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO teamkills (id, teamkiller, victim, game_id, gametime, reported_at) VALUES
  (1, 2, 1, 1, 5000, NOW());

