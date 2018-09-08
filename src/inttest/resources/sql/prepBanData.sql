DELETE FROM ban_revoke;
DELETE FROM ban;

INSERT INTO login (id, login, email, password) VALUES
  (4, 'BANNED', 'banned@faforever.com', 'not relevant');

INSERT INTO ban (id, player_id, author_id, reason, expires_at, level) VALUES
  (1, 4, 2, 'Test permaban', DATE_ADD(NOW(), INTERVAL 1 DAY), 'GLOBAL'),
  (2, 2, 2, 'To be revoked ban', DATE_ADD(NOW(), INTERVAL 1 DAY), 'GLOBAL');

INSERT INTO ban_revoke (ban_id, reason, author_id) VALUES
  (2, 'Test revoke', 2);

