SET FOREIGN_KEY_CHECKS = 0;

DELETE
FROM avatars;
DELETE
FROM avatars_list;
DELETE
FROM group_permission;
DELETE
FROM group_permission_assignment;

SET FOREIGN_KEY_CHECKS = 1;


INSERT INTO avatars_list (id, filename, tooltip)
VALUES (1, 'avatar1.png', 'Avatar No. 1'),
       (2, 'avatar2.png', 'Avatar No. 2'),
       (3, 'donator.png', 'Donator Avatar');

INSERT INTO avatars (id, idUser, idAvatar, selected)
VALUES (1, 5, 1, 1),
       (2, 5, 2, 0);

INSERT INTO group_permission(id, technical_name, name_key)
VALUES (1, 'UPDATE_AVATAR', 'UPDATE_AVATAR');

INSERT INTO group_permission_assignment (group_id, permission_id)
VALUES (2, 1); -- MODERATOR -> UPDATE_AVATAR

COMMIT;
