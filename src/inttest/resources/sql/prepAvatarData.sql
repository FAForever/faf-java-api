INSERT INTO avatars_list (id, filename, tooltip)
VALUES (1, 'avatar1.png', 'Avatar No. 1'),
       (2, 'avatar2.png', 'Avatar No. 2'),
       (3, 'donator.png', 'Donator Avatar'),
       (4, 'avatar space.png', 'Space Avatar');

INSERT INTO avatars (id, idUser, idAvatar, selected)
VALUES (1, 5, 1, 1),
       (2, 5, 2, 0);

INSERT INTO group_permission_assignment (group_id, permission_id)
VALUES (2, 9); -- MODERATOR -> UPDATE_AVATAR
