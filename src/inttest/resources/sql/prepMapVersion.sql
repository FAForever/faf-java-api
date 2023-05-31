INSERT INTO map (id, display_name, map_type, battle_type, author, recommended, license) VALUES (1, 'display name', 'mtype', 'btype', 1, false, 1);
INSERT INTO map_version (id, description, max_players, width, height, version, filename, map_id, hidden, ranked)
VALUES (1, 'des', 2, 2, 2, 1, 'map/ghb.zip', 1, 0, 1);
INSERT INTO map_reviews_summary (id, map_id, positive, negative, score, reviews, lower_bound)
VALUES (1, 1, 0, 0, 2, 1, 0);
INSERT INTO map_version_reviews_summary (id, map_version_id, positive, negative, score, reviews, lower_bound)
VALUES (1, 1, 0, 0, 0, 0, 0);
