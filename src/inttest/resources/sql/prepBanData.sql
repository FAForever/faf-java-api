DELETE FROM ban;

INSERT INTO login (id, login, email, password) VALUES
  (4, 'BANNED', 'banned@faforever.com', 'not relevant');

INSERT INTO ban (id, player_id, author_id, reason, expires_at, level) VALUE
  (1, 4, 1, 'Test permaban', DATE_ADD(NOW(), INTERVAL 1 DAY), 'GLOBAL');
INSERT INTO ban (id, player_id, author_id, reason, expires_at, level, revoke_time, revoke_author_id, revoke_reason)
  VALUE
  (2, 2, 1, 'To be revoked ban', DATE_ADD(NOW(), INTERVAL 1 DAY), 'GLOBAL', NOW(), 1, 'Test revoke');
