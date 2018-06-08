DELETE FROM tutorial;
DELETE FROM tutorial_category;
DELETE FROM messages;

SET FOREIGN_KEY_CHECKS = 0;
INSERT INTO tutorial_category (id, category_key) VALUES (1, 'category');
INSERT INTO tutorial (id, title_key, description_key, category, image, ordinal, launchable, technical_name) VALUES
  (1, 'title', 'description', 1, 'image.png', 1, 1, 'tec name');
INSERT INTO messages (`key`, language, region, value) VALUES ('title', 'en', 'US', 'title');
INSERT INTO messages (`key`, language, region, value) VALUES ('description', 'en', 'US', 'description');
INSERT INTO messages (`key`, language, region, value) VALUES ('category', 'en', 'US', 'category');
