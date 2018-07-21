DELETE FROM winner_for_voting_question;
DELETE FROM voting_answer;
DELETE FROM vote;
DELETE FROM voting_choice;
DELETE FROM voting_question;
DELETE FROM voting_subject;

INSERT INTO voting_subject (id, subject_key, begin_of_vote_time, end_of_vote_time, min_games_to_vote, description_key, topic_url)
VALUES
  (1, 'subject', NOW() - INTERVAL 1 MINUTE, DATE_ADD(NOW(), INTERVAL 1 YEAR), 0, 'des', 'www.google.de');

INSERT INTO voting_subject (id, subject_key, reveal_winner, begin_of_vote_time, end_of_vote_time, min_games_to_vote, description_key, topic_url)
VALUES
  (2, 'subject', 0, DATE_SUB(NOW(), INTERVAL 1 YEAR), DATE_SUB(NOW(), INTERVAL 45 DAY), 0, 'des', 'www.google.de');

INSERT INTO voting_question (id, max_answers, question_key, voting_subject_id, description_key, ordinal, alternative_voting)
VALUES
  (1, 2, 'question', 1, 'des', 1, 1);

INSERT INTO voting_choice (id, choice_text_key, voting_question_id, description_key, ordinal) VALUES
  (1, 'text', 1, 'des', 1);

INSERT INTO voting_choice (id, choice_text_key, voting_question_id, description_key, ordinal) VALUES
  (2, 'text', 1, 'des', 2);

INSERT INTO vote (id, player_id, voting_subject_id) VALUES
  (1, 1, 1);

INSERT INTO voting_question (id, max_answers, question_key, voting_subject_id, description_key, ordinal, alternative_voting)
VALUES
  (2, 2, 'question', 2, 'des', 1, 1);

INSERT INTO voting_choice (id, choice_text_key, voting_question_id, description_key, ordinal) VALUES
  (3, 'text', 2, 'des', 2);

INSERT INTO vote (id, player_id, voting_subject_id) VALUES
  (2, 3, 2);

INSERT INTO voting_answer (id, vote_id, voting_choice_id, alternative_ordinal) VALUES
  (1, 2, 3, 0);
