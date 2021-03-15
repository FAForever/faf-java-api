INSERT INTO `mod` (id, display_name, author, uploader)
VALUES (1, 'MOD_001', 'some Author', 1),
       (2, 'MOD_002', 'some Author', 1),
       (3, 'Total Annihilation Music', 'some Author', 1);

INSERT INTO mod_version (id, description, type, uid, version, filename, hidden, mod_id)
VALUES (1, 'SCMP 001', 'SIM', '11111111-1111-1111-1111-111111111111', 1, 'maps/scmp_001.v0001.zip', false, 1),
       (2, 'SCMP 002', 'SIM', '22222222-2222-2222-2222-222222222222', 1, 'maps/scmp_002.v0001.zip', false, 2);

