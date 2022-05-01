| Env variable  | Default value | Optional if env is defined | Default value (local) |
| ---- | ---- | ---- | ---- |
| ACTIVATION_URL_FORMAT | `https://www.${FAF_DOMAIN}/account/activate?username=%s&token=%s` | `FAF_DOMAIN` | `http://localhost:8020/account/activate?username=%s&token=%s` |
| ANOPE_DATABASE_NAME | `faf-anope` |  |  |
| API_PORT | `8010` |  |  |
| API_PROFILE | `local` |  |  |
| AVATAR_ALLOWED_FILE_EXTENSIONS | `png` |  | `png` |
| AVATAR_DOWNLOAD_URL_FORMAT | `https://content.${FAF_DOMAIN}/faf/avatars/%s` | `FAF_DOMAIN` | `https://content.test.faforever.com/faf/avatars/%s` |
| AVATAR_IMAGE_HEIGHT | `20` |  |  |
| AVATAR_IMAGE_WIDTH | `40` |  |  |
| AVATAR_MAX_SIZE_BYTES | `4096` |  |  |
| AVATAR_TARGET_DIRECTORY | `/content/avatars` |  | `build/cache/avatars` |
| CHALLONGE_KEY |  |  |  |
| CLAN_INVITE_LINK_EXPIRE_DURATION_MINUTES | `604800` |  |  |
| CLAN_WEBSITE_URL_FORMAT | `https://clans.${FAF_DOMAIN}/clan/%s` | `FAF_DOMAIN` | `http://clans.test.faforever.com/clan/%s` |
| CONTEXT_PATH | `/` |  |  |
| DATABASE_ADDRESS |  |  | `127.0.0.1` |
| DATABASE_NAME |  |  | `faf` |
| DATABASE_PASSWORD |  |  | `banana` |
| DATABASE_SCHEMA_VERSION | `123` |  |  |
| DATABASE_USERNAME |  |  | `faf-java-api` |
| EMAIL_FROM_ADDRESS |  |  | `faf@example.com` |
| EMAIL_FROM_NAME |  |  | `FAForever` |
| EXE_UPLOAD_BETA_PATH | `/content/legacy-featured-mod-files/updates_fafbeta_files` |  | `build/exe/beta` |
| EXE_UPLOAD_DEVELOP_PATH | `/content/legacy-featured-mod-files/updates_fafdevelop_files` |  | `build/exe/develop` |
| FEATURED_MODS_TARGET_DIRECTORY | `/content/legacy-featured-mod-files` |  | `build/cache/deployment` |
| FEATURED_MOD_URL_FORMAT | `https://content.${FAF_DOMAIN}/legacy-featured-mod-files/%s/%s` | `FAF_DOMAIN` | `https://content.test.faforever.com/faf/updaterNew/%s/%s` |
| FORGED_ALLIANCE_EXE_PATH | `/content/legacy-featured-mod-files/updates_faf_files/ForgedAlliance.exe` |  |  |
| GITHUB_ACCESS_TOKEN | `false` |  |  |
| GITHUB_DEPLOYMENT_ENVIRONMENT | `production` |  | `development` |
| GITHUB_WEBHOOK_SECRET | `false` |  |  |
| GOG_GAMES_LIST_URL | `https://www.gog.com/u/%s/games/stats?sort=recent_playtime&order=desc&page=%d` |  |  |
| GOG_PROFILE_URL | `https://www.gog.com/u/%s` |  |  |
| GOG_TOKEN_FORMAT | `{{FAF:%s}}` |  |  |
| JWT_FAF_HYDRA_ISSUER | `https://hydra.${FAF_DOMAIN}/` | `FAF_DOMAIN` | `https://hydra.test.faforever.com/` |
| JWT_FAF_HYDRA_JWKS_URL | `https://hydra.${FAF_DOMAIN}/.well-known/jwks.json` | `FAF_DOMAIN` | `https://hydra.test.faforever.com/.well-known/jwks.json` |
| JWT_PRIVATE_KEY_PATH | `/pki/secret.key` |  | `test-pki-private.key` |
| JWT_PUBLIC_KEY_PATH | `/pki/public.key` |  | `test-pki-public.key` |
| LEAGUE_DATABASE_ADDRESS | `127.0.0.1` |  | `127.0.0.1` |
| LEAGUE_DATABASE_NAME | `faf-league` |  | `faf-league` |
| LEAGUE_DATABASE_PASSWORD |  |  | `banana` |
| LEAGUE_DATABASE_USERNAME | `faf-league-service` |  | `faf-league-service` |
| LOG_FILE_DIRECTORY |  |  |  |
| LOG_LEVEL | `info` |  |  |
| MAIL_HOST | `false` |  |  |
| MAIL_PASSWORD | `false` |  |  |
| MAIL_PORT | `false` |  |  |
| MAIL_USERNAME | `false` |  |  |
| MANAGEMENT_ADDRESS |  |  |  |
| MANAGEMENT_PORT | `8011` |  |  |
| MAP_DOWNLOAD_URL_FORMAT | `https://content.${FAF_DOMAIN}/maps/%s` | `FAF_DOMAIN` | `https://content.test.faforever.com/maps/%s` |
| MAP_LARGE_PREVIEWS_URL_FORMAT | `https://content.${FAF_DOMAIN}/maps/previews/large/%s` | `FAF_DOMAIN` | `https://content.test.faforever.com/faf/maps/previews/large/%s` |
| MAP_PREVIEW_PATH_LARGE | `/content/maps/previews/large` |  | `build/cache/map_previews/large` |
| MAP_PREVIEW_PATH_SMALL | `/content/maps/previews/small` |  | `build/cache/map_previews/small` |
| MAP_SMALL_PREVIEWS_URL_FORMAT | `https://content.${FAF_DOMAIN}/maps/previews/small/%s` | `FAF_DOMAIN` | `https://content.test.faforever.com/faf/maps/previews/small/%s` |
| MAP_UPLOAD_PATH | `/content/maps` |  | `build/cache/map/maps` |
| MOD_DOWNLOAD_URL_FORMAT | `https://content.${FAF_DOMAIN}/mods/%s` | `FAF_DOMAIN` | `https://content.test.faforever.com/faf/vault/mods/%s` |
| MOD_PREVIEW_URL_FORMAT | `https://content.${FAF_DOMAIN}/mods/%s` | `FAF_DOMAIN` | `https://content.test.faforever.com/faf/vault/mods/%s` |
| NODEBB_ADMIN_USERID | `1` |  |  |
| NODEBB_BASE_URL | `false` |  |  |
| NODEBB_MASTER_TOKEN | `false` |  |  |
| PASSWORD_RESET_EMAIL_BODY | `Reset email body for user {0} with reset link {1}` |  | `Reset email body for user {0} with reset link {1}` |
| PASSWORD_RESET_EMAIL_SUBJECT | `FAF password reset` |  | `FAF password reset` |
| PASSWORD_RESET_URL_FORMAT | `https://www.${FAF_DOMAIN}/account/password/confirmReset?username=%s&token=%s` | `FAF_DOMAIN` | `http://localhost:8020/account/password/confirmReset?username=%s&token=%s` |
| PID | `- ` |  |  |
| RECAPTCHA_ENABLED | `false` |  | `false` |
| RECAPTCHA_SECRET |  |  |  |
| REGISTRATION_EMAIL_BODY | `"Registration email body for user {0} with activation link {1}"` |  | `"Registration email body for user {0} with activation link {1}"` |
| REGISTRATION_EMAIL_SUBJECT | `FAF user registration` |  | `FAF user registration` |
| REPLAY_DOWNLOAD_URL_FORMAT | `https://replays.${FAF_DOMAIN}/%s` | `FAF_DOMAIN` | `https://content.test.faforever.com/replays/%s` |
| REPOSITORIES_DIRECTORY | `/repositories` |  | `build/cache/repos` |
| STEAM_API_KEY |  |  | `banana` |
| STEAM_LINK_REDIRECT_URL_FORMAT | `https://api.${FAF_DOMAIN}/users/linkToSteam?token=%s` | `FAF_DOMAIN` | `http://localhost:8010/users/linkToSteam?token=%s` |
| STEAM_PASSWORD_RESET_REDIRECT_URL_FORMAT | `https://api.${FAF_DOMAIN}/users/requestPasswordResetViaSteam` | `FAF_DOMAIN` | `http://localhost:8010/users/requestPasswordResetViaSteam` |
| STEAM_REALM | `https://api.${FAF_DOMAIN}` | `FAF_DOMAIN` | `http://localhost` |
| TESTING_EXE_UPLOAD_KEY |  |  | `banana` |
| TUTORIAL_THUMBNAIL_URL_FORMAT | `https://content.${FAF_DOMAIN}/faf/tutorials/thumbs/%s` | `FAF_DOMAIN` | `https://content.test.faforever.com/faf/tutorials/thumbs/%s` |
