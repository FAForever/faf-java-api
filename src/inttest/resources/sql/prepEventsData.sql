INSERT INTO event_definitions (id, name_key, image_url, type)
VALUES ('15b6c19a-6084-4e82-ada9-6c30e282191f', 'event.seraphimWins', null, 'NUMERIC');
INSERT INTO event_definitions (id, name_key, image_url, type)
VALUES ('225e9b2e-ae09-4ae1-a198-eca8780b0fcd', 'event.lostAirUnits', null, 'NUMERIC');
INSERT INTO event_definitions (id, name_key, image_url, type)
VALUES ('cc791f00-343c-48d4-b5b3-8900b83209c0', 'event.secondsPlayed', null, 'TIME');
INSERT INTO player_events (id, player_id, event_id, count, create_time, update_time)
VALUES (1, 1, '15b6c19a-6084-4e82-ada9-6c30e282191f', 21, '2019-01-06 10:48:18', '2019-01-06 10:48:18');
INSERT INTO player_events (id, player_id, event_id, count, create_time, update_time)
VALUES (2, 1, '225e9b2e-ae09-4ae1-a198-eca8780b0fcd', 10, '2019-01-06 13:36:54', '2019-01-06 13:36:54');
