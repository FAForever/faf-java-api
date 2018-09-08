DELETE FROM ban_revoke;
DELETE FROM ban;

INSERT INTO login (id, login, email, password) VALUES
  (4, 'BANNED', 'banned@faforever.com', 'not relevant');

INSERT INTO ban (id, player_id, author_id, reason, expires_at, level) VALUES
  (1, 4, 2, 'Test permaban', DATE_ADD(NOW(), INTERVAL 1 DAY), 'GLOBAL');
