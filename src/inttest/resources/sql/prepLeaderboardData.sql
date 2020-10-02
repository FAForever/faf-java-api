insert into ladder1v1_rating_rank_view (id, ranking, mean, deviation, num_games, win_games, rating)
VALUES (1, 1, 1500, 120, 5, 1, 1500 - 3 * 120),
       (2, 2, 1200, 90, 5, 2, 1200 - 3 * 90),
       (3, 3, 1000, 100, 5, 3, 1000 - 3 * 100);

insert into global_rating_rank_view (id, ranking, mean, deviation, num_games, rating)
VALUES (1, 1, 1500, 120, 5, 1500 - 3 * 120),
       (2, 2, 1200, 90, 5, 1200 - 3 * 90),
       (3, 3, 1000, 100, 5, 1000 - 3 * 100);


commit;
