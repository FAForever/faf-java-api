INSERT INTO `mod` (id, display_name, author, uploader, recommended, license)
VALUES (1, 'MOD_001', 'some Author', 1, false, 1),
       (2, 'MOD_002', 'some Author', 1, false, 1);

INSERT INTO mod_version (id, description, type, uid, version, filename, hidden, mod_id)
VALUES (1, 'SCMP 001', 'SIM', '11111111-1111-1111-1111-111111111111', 1, 'maps/scmp_001.v0001.zip', false, 1),
       (2, 'SCMP 002', 'SIM', '22222222-2222-2222-2222-222222222222', 1, 'maps/scmp_002.v0001.zip', false, 2);


INSERT INTO mod_reviews_summary (id, mod_id, positive, negative, score, reviews, lower_bound)
VALUES (1, 1, 0, 0, 2, 1, 0);
INSERT INTO mod_version_reviews_summary (id, mod_version_id, positive, negative, score, reviews, lower_bound)
VALUES (1, 1, 0, 0, 0, 0, 0);
