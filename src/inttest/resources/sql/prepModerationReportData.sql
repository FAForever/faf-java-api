DELETE FROM reported_user;
DELETE FROM moderation_report;

INSERT INTO moderation_report (id, reporter_id, report_description, game_id, report_status, game_incident_timecode, moderator_notice, moderator_private_note, last_moderator)
VALUES (1, 5, 'Report description', null, 'AWAITING', 'Incident timecode', null, null, null),
       (2, 5, 'Report description', 1, 'PROCESSING', 'Incident timecode', 'Moderator notice', 'Moderator private note',
        2),
  (3, 3, 'Report description', null, 'AWAITING', 'Incident timecode', null, null, null);

INSERT INTO reported_user (id, player_id, report_id) VALUES
  (1, 3, 1),
  (2, 3, 2);
COMMIT;
