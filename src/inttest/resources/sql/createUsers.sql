DELETE FROM oauth_clients;
DELETE FROM login;

INSERT INTO oauth_clients (id, name, client_secret, client_type, redirect_uris, default_redirect_uri, default_scope)
VALUES ('test', 'test', 'test', 'public', 'http://localhost', 'http://localhost',
        'read_events read_achievements upload_map upload_mod write_account_data');

INSERT INTO login (id, login, email, password)
VALUES (1, 'USER', 'user@faforever.com', '92b7b421992ef490f3b75898ec0e511f1a5c02422819d89719b20362b023ee4f');
INSERT INTO login (id, login, email, password)
VALUES (2, 'MODERATOR', 'moderator@faforever.com', '778ac5b81fa251b450f827846378739caee510c31b01cfa9d31822b88bed8441');
INSERT INTO login (id, login, email, password)
VALUES (3, 'ADMIN', 'admin@faforever.com', '835d6dc88b708bc646d6db82c853ef4182fabbd4a8de59c213f2b5ab3ae7d9be');

