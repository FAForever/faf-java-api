INSERT INTO map_pool (id, name) VALUES
  (1, 'Ladder 1v1 <300'),
  (2, 'Ladder 1v1 300-800'),
  (3, 'Ladder 1v1 800-1300'),
  (4, 'Ladder 1v1 1300-1800'),
  (5, 'Ladder 1v1 1800+');

INSERT INTO matchmaker_queue_map_pool (id, matchmaker_queue_id, map_pool_id, min_rating, max_rating, veto_tokens_per_player, max_tokens_per_map, minimum_maps_after_veto) VALUES
  (100, 1, 1, null, 300,1,1,1),
  (101, 1, 2, 300, 800,1,1,1.5),
  (102, 1, 3, 800, 1300,1,1,1),
  (103, 1, 4, 1300, 1800,1,1,1),
  (104, 1, 5, 1800, null,1,1,1);
