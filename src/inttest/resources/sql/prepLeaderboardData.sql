DELETE FROM global_rating;
DELETE FROM ladder1v1_rating;

insert into ladder1v1_rating (id, mean, deviation, numGames, winGames, is_active) VALUES
  (1, 1500, 120, 5, 1, 1),
  (2, 1200, 90, 5, 2, 1),
  (3, 1000, 100, 5, 3, 1);

insert into global_rating (id, mean, deviation, numGames, is_active) VALUES
  (1, 1500, 120, 5, 1),
  (2, 1200, 90, 5, 1),
  (3, 1000, 100, 5, 1);


commit;
