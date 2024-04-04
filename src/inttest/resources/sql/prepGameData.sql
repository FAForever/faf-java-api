INSERT INTO game_stats (id, startTime, gameName, gameType, gameMod, `host`, mapId, validity, replay_available)
VALUES (1, NOW(), 'Test game', 'DEMORALIZATION', 6, 1, 1, 0, false);

insert into game_review (id, text, user_id, score, game_id) VALUES (1, 'Awesome', 1, 5, 1);
insert into game_review (id, text, user_id, score, game_id) VALUES (2, 'Nice', 2, 3, 1);
insert into game_review (id, text, user_id, score, game_id) VALUES (3, 'Meh', 3, 2, 1);
