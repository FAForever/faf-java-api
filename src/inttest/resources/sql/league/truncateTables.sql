SET FOREIGN_KEY_CHECKS = 0;

DELETE
FROM `league_season_score`;
DELETE
FROM `league_season_division_subdivision`;
DELETE
FROM `league_season_division`;
DELETE
FROM `league_season`;
DELETE
FROM `league`;
DELETE
FROM `leaderboard`;

SET FOREIGN_KEY_CHECKS = 1;
