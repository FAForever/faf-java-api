INSERT INTO game_stats (id, startTime, gameName, gameType, gameMod, `host`, mapId, validity, replay_available)
VALUES (1, NOW(), 'Test game', '0', 6, 1, 1, 0, false);

INSERT INTO game_player_stats (id, gameId, playerId, AI, faction, color, team, place, mean, deviation, after_mean,
                               after_deviation, score, scoreTime, result)
VALUES (1, 1, 1, FALSE, 0, 0, 0, 0, 1000.0, 100.0, null, null, null, null, null);
