INSERT INTO avatars_list (id, filename, tooltip, avatar_text_description)
VALUES (1, 'avatar1.png', 'Avatar No. 1', null),
       (2, 'avatar2.png', 'Avatar No. 2', null),
       (3, 'donator.png', 'Donator Avatar', 'Only for donators'),
       (4, 'avatar space.png', 'Space Avatar', null);

INSERT INTO avatars (id, idUser, idAvatar, selected)
VALUES (1, 5, 1, 1),
       (2, 5, 2, 0);

INSERT INTO group_permission_assignment (group_id, permission_id)
VALUES (2, 9); -- MODERATOR -> UPDATE_AVATAR
