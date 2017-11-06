DELETE FROM ladder_map;
DELETE FROM map_version;
DELETE FROM map;

INSERT INTO map (id, display_name, map_type, battle_type, author) VALUES
  (1, 'SCMP_001', 'FFA', 'skirmish', 1),
  (2, 'SCMP_002', 'FFA', 'skirmish', 1);

INSERT INTO map_version (id, description, max_players, width, height, version, filename, hidden, map_id) VALUES
  (1, 'SCMP 001', 8, 5, 5, 1, 'maps/scmp_001.v0001.zip', 0, 1),
  (2, 'SCMP 002', 8, 5, 5, 1, 'maps/scmp_002.v0001.zip', 0, 2);

INSERT INTO ladder_map (id, idmap) VALUES
  (1, 1);
