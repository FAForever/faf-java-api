-- game_featuredMods is populated by R__010_game_featuredMods.sql from Flyway
INSERT INTO `updates_faf` (`id`, `filename`, `path`) VALUES
(1,	'ForgedAlliance.exe',	'bin');

INSERT INTO `updates_faf_files` (`id`, `fileId`, `version`, `name`, `md5`, `obselete`) VALUES
(1703,	1,	3706,	'ForgedAlliance.3706.exe',	'c20b922a785cf5876c39b7696a16f162',	0);

INSERT INTO `updates_fafbeta` (`id`, `filename`, `path`) VALUES
(1,	'ForgedAlliance.exe',	'bin');

INSERT INTO `updates_fafbeta_files` (`id`, `fileId`, `version`, `name`, `md5`, `obselete`) VALUES
(1703,	1,	3706,	'ForgedAlliance.3706.exe',	'c20b922a785cf5876c39b7696a16f162',	0);

INSERT INTO `updates_fafdevelop` (`id`, `filename`, `path`) VALUES
(1,	'ForgedAlliance.exe',	'bin');

INSERT INTO `updates_fafdevelop_files` (`id`, `fileId`, `version`, `name`, `md5`, `obselete`) VALUES
(4327,	1,	3707,	'ForgedAlliance.3707.exe',	'79f0ea70625ab464d369721183e9fd29',	0);
