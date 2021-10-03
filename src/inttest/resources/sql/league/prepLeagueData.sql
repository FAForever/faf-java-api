INSERT INTO leaderboard (id, technical_name)
VALUES (1, 'leaderboard1'),
       (2, 'leaderboard2');

INSERT INTO league (id, technical_name, name_key, description_key)
VALUES (1, 'league1', 'league_name_1', 'league_description_1'),
       (2, 'league2', 'league_name_2', 'league_description_2');

INSERT INTO league_season (id, league_id, leaderboard_id, name_key, start_date, end_date)
VALUES (1, 1, 1, 'season1', NOW(), DATE_ADD(NOW(), INTERVAL 1 MONTH)),
       (2, 2, 2, 'season2', DATE_SUB(NOW(), INTERVAL 1 MONTH), DATE_SUB(NOW(), INTERVAL 1 DAY));

INSERT INTO league_season_division (id, league_season_id, division_index, name_key, description_key)
VALUES (1, 1, 1, 'division_name_1', 'division_description_1'),
       (2, 1, 2, 'division_name_2', 'division_description_2'),
       (3, 2, 3, 'division_name_3', 'division_description_3'),
       (4, 2, 4, 'division_name_4', 'division_description_4');

INSERT INTO league_season_division_subdivision (id, league_season_division_id, subdivision_index, name_key,
                                                description_key, min_rating, max_rating, highest_score)
VALUES (1, 1, 1, 'subdivision_name_1', 'subdivision_description_1', 0.0, 1000.0, 50),
       (2, 1, 2, 'subdivision_name_2', 'subdivision_description_2', 1000.0, 2000.0, 1050),
       (3, 2, 3, 'subdivision_name_3', 'subdivision_description_3', 0.0, 500.0, 50),
       (4, 2, 4, 'subdivision_name_4', 'subdivision_description_4', 500.0, 1000.0, 550);

INSERT INTO league_season_score (id, league_season_id, subdivision_id, login_id, score, game_count)
VALUES (1, 1, 1, 1, 10, 10),
       (2, 1, 1, 2, 11, 10),
       (3, 1, 1, 3, 12, 10),
       (4, 1, 1, 4, 13, 10),
       (5, 1, 2, 5, 20, 20),
       (6, 1, 2, 6, 21, 20),
       (7, 1, 2, 7, 22, 20),
       (8, 1, 2, 8, 23, 20);
