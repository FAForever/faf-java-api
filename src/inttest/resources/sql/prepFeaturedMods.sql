DELETE FROM updates_fafdevelop_files;
DELETE FROM updates_fafdevelop;
DELETE FROM updates_fafbeta_files;
DELETE FROM updates_fafbeta;
DELETE FROM reported_user;
DELETE FROM moderation_report;
DELETE FROM game_stats;
DELETE FROM game_featuredMods;

INSERT INTO `game_featuredMods` (`id`, `gamemod`, `description`, `name`, `publish`, `order`, `git_url`, `git_branch`, `file_extension`, `allow_override`, `deployment_webhook`) VALUES
(1,	'faf',	'Forged Alliance Forever',	'FAF',	1,	0,	'https://github.com/FAForever/fa.git',	'deploy/faf',	'nx2',	0,	NULL),
(27,	'fafbeta',	'Beta version of the next FAF patch',	'FAF Beta',	1,	2,	'https://github.com/FAForever/fa.git',	'deploy/fafbeta',	'nx4',	1,	NULL),
(28,	'fafdevelop',	'Updated frequently for testing the upcoming game Patch',	'FAF Develop',	1,	11,	'https://github.com/FAForever/fa.git',	'deploy/fafdevelop',	'nx5',	1,	NULL);

INSERT INTO `updates_fafbeta` (`id`, `filename`, `path`) VALUES
(1,	'ForgedAlliance.exe',	'bin');

INSERT INTO `updates_fafbeta_files` (`id`, `fileId`, `version`, `name`, `md5`, `obselete`) VALUES
(1703,	1,	3706,	'ForgedAlliance.3706.exe',	'c20b922a785cf5876c39b7696a16f162',	0);

INSERT INTO `updates_fafdevelop` (`id`, `filename`, `path`) VALUES
(1,	'ForgedAlliance.exe',	'bin');

INSERT INTO `updates_fafdevelop_files` (`id`, `fileId`, `version`, `name`, `md5`, `obselete`) VALUES
(4327,	1,	3707,	'ForgedAlliance.3707.exe',	'79f0ea70625ab464d369721183e9fd29',	0);
