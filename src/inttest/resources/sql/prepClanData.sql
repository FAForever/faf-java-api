DELETE FROM clan_membership;
DELETE FROM clan;

INSERT INTO login (id, login, email, password) VALUES
  (10, 'CLAN_FOUNDER', 'clan_founder@faforever.com', 'not used'),
  (11, 'CLAN_LEADER', 'clan_leader@faforever.com', 'not used'),
  (12, 'CLAN_MEMBER', 'clan_member@faforever.com', 'not used'),
  (13, 'CLAN_MEMBER_B', 'clan_member_b@faforever.com', 'not used');

INSERT INTO clan (id, name, tag, founder_id, leader_id, description) VALUES
  (1, 'Alpha Clan', '123', 10, 11, 'Lorem ipsum dolor sit amet, consetetur sadipscing elitr'),
  (2, 'Beta Clan', '345', 1, 1, 'Sed diam nonumy eirmod tempor invidunt ut labore');

INSERT INTO clan_membership (id, clan_id, player_id) VALUES
  (1, 1, 11),
  (2, 1, 12),
  (3, 1, 13),
  (4, 2, 10);
