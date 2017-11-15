DELETE FROM avatars;
DELETE FROM avatars_list;

INSERT INTO avatars_list (id, url, tooltip) VALUES
  (1, 'http://localhost/avatar1.png', 'Avatar No. 1'),
  (2, 'http://localhost/avatar2.png', 'Avatar No. 2');

INSERT INTO avatars (id, idUser, idAvatar, selected) VALUES
  (1, 1, 2, 1),
  (2, 2, 2, 0);
