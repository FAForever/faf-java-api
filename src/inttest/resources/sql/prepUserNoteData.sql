DELETE FROM ban_revoke;
DELETE FROM ban;

INSERT INTO login (id, login, email, password) VALUES
  (4, 'BANNED', 'banned@faforever.com', 'not relevant');

INSERT INTO user_notes (id, user_id, author, watched, note) VALUES
  (1, 4, 1, 0, 'Test user note');
