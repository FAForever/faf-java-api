package com.faforever.api.user;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.data.domain.User;
import com.faforever.api.error.ApiException;
import com.faforever.api.error.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static com.faforever.api.error.ApiExceptionMatcher.hasErrorCode;
import static com.faforever.api.user.GogService.GOG_FA_GAME_ID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GogServiceTest {

  public static final String GOG_USERNAME = "someUsername";
  public static final String PROFILE_PAGE_URL = "profilePageUrl";
  public static final String GAME_LIST_PAGE_URL = "gameListUrl?user=%s&page=%s";
  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private FafApiProperties fafApiProperties;

  @Mock
  private RestTemplate restTemplate;

  @Mock
  private User user;

  private GogService instance;

  @BeforeEach
  void setUp() {
    instance = new GogService(fafApiProperties, restTemplate);
  }

  @Test
  void buildGogToken(@Mock User user) {
    when(fafApiProperties.getGog().getTokenFormat()).thenReturn("{{FAF:%s}}");
    when(user.getId()).thenReturn(12345);
    assertThat(instance.buildGogToken(user), is("{{FAF:12345}}"));
  }

  @ParameterizedTest
  @CsvSource({
    "false,a",
    "false,aa",
    "false,1234567891113151719212325272931",
    "false,!§$%&",
    "false,öäüeeeöüä",
    "true,aaa",
    "true,AAA",
    "true,Brutus5000",
  })
  void verifyGogUsername(boolean allowed, String name) {
    assertThat(instance.verifyGogUsername(name), is(allowed));
  }

  @Test
  void verifyProfileTokenWithPrivateProfile() {
    when(fafApiProperties.getGog().getProfilePageUrl()).thenReturn(PROFILE_PAGE_URL);

    when(restTemplate.getForObject(PROFILE_PAGE_URL, String.class))
      .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "", HttpHeaders.EMPTY, null, null));

    ApiException result = assertThrows(ApiException.class, () -> instance.verifyProfileToken(GOG_USERNAME, user, "targetToken"));
    assertThat(result, hasErrorCode(ErrorCode.GOG_LINK_PROFILE_NOT_PUBLIC));
  }

  @Test
  void verifyProfileTokenWithGogInternalServerError() {
    when(fafApiProperties.getGog().getProfilePageUrl()).thenReturn(PROFILE_PAGE_URL);

    when(restTemplate.getForObject(PROFILE_PAGE_URL, String.class))
      .thenThrow(HttpClientErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR, "", HttpHeaders.EMPTY, null, null));

    ApiException result = assertThrows(ApiException.class, () -> instance.verifyProfileToken(GOG_USERNAME, user, "targetToken"));
    assertThat(result, hasErrorCode(ErrorCode.GOG_LINK_INTERNAL_SERVER_ERROR));
  }

  @Test
  void verifyProfileTokenWithGogProfileEmpty() {
    when(fafApiProperties.getGog().getProfilePageUrl()).thenReturn(PROFILE_PAGE_URL);

    when(restTemplate.getForObject(PROFILE_PAGE_URL, String.class))
      .thenReturn("");

    ApiException result = assertThrows(ApiException.class, () -> instance.verifyProfileToken(GOG_USERNAME, user, "targetToken"));
    assertThat(result, hasErrorCode(ErrorCode.GOG_LINK_PROFILE_NOT_PUBLIC));
  }

  @Test
  void verifyProfileTokenSuccess() {
    when(fafApiProperties.getGog().getProfilePageUrl()).thenReturn(PROFILE_PAGE_URL);

    when(restTemplate.getForObject(PROFILE_PAGE_URL, String.class))
      .thenReturn("""
        <html>
        <head>
        </head>
        <body>
        	<script
        		src="https://profiles-static.gog.com/assets/vendor/anchorme_min.js?afebd1f7c7ea2891da546c75c2e25f33c0cbbff3">
        	</script>
        	<script
        		src="https://profiles-static.gog.com/assets/vendor/sanitize-html_min.js?afebd1f7c7ea2891da546c75c2e25f33c0cbbff3">
        	</script>
        	<script>
        		// define global namespace object
                    window.profilesData = window.profilesData || {};

                    window.profilesData.env = "prod";

                    window.profilesData.currency = "USD";
                    window.profilesData.langCode = "en-US";
                    window.profilesData.translations = {"activity_achievement_action_text_one":"earned 1 achievement in %productLink%","activity_achievement_action_text_few":"earned %achievementsCount% achievements in %productLink%","activity_achievement_action_text_many":"earned %achievementsCount% achievements in %productLink%","activity_achievement_action_text_other":"earned %achievementsCount% achievements in %productLink%","activity_achievements_milestone_action_text":"reached a milestone in %productLink%","activity_custom_post_action_text":"shared a thought","activity_first_time_played_milestone_action_text":"started playing %productLink%","activity_forum_topic_created_action_text":"started a topic in the %forumLink% forum","activity_playtime_milestone_action_text":"reached a milestone in %productLink%","activity_product_review_action_text":"reviewed %productLink%","user_status_online":"Online","user_status_offline":"Offline","filter_recent_playtime":"Recently played","filter_achievements":"Achievements earned","filter_total_playtime":"Total playtime","filter_abc":"Alphabetically","filter_rarity_desc":"Common first","filter_rarity_asc":"Legendary first","filter_original_order":"Developer's order","filter_by_name":"Username","filter_by_status":"Online first","filter_by_total_games":"Owned games","filter_by_total_achievements":"Achievements earned","filter_by_total_playtime":"Total playtime","filter_unlock_date":"Unlock date","filter_you":"(You)"};
                    window.profilesData.sortingOptions = {alphabetically: "filter_abc",
                                                          percent_of_unlocked_achievements: "filter_achievements",
                                                          total_playtime: "filter_total_playtime",
                                                          recent_playtime: "filter_recent_playtime",
                                                          user_unlock_date: "filter_unlock_date",
                                                          rarityDESC: "filter_rarity_desc",
                                                          rarityASC: "filter_rarity_asc",
                                                          original: "filter_original_order",
                                                          username: "filter_by_name",
                                                          status: "filter_by_status",
                                                          gamesCount: "filter_by_total_games",
                                                          achievementsCount: "filter_by_total_achievements" ,
                                                          playtime: "filter_by_total_playtime"
                                                         };

                                    window.profilesData.currentUser = null;
                                                window.profilesData.currentUserPreferences = null;
                                                window.profilesData.profileUser = {"username":"Brutus5000","created_date":"","userId":"","avatar":"https:\\/\\/images.gog.com\\/67ec2f3bd8d73b975a8e8d8905da5211ccdf1ee4505f0a8e32df1a355df3652f.jpg","settings":{"allow_to_be_invited_by":"friend"},"stats":{"games_owned":666,"achievements":null,"hours_played":0},"background":{"id":"100","name":"Sea-bg","type":"predefined","src":"https:\\/\\/images.gog.com\\/b78feb5921e6a290638ba7e53927ec1db9dcb0686cb655b351c06fda0fd07990.jpg","background_dominant_color":[58,118,126]}};
                                                window.profilesData.profileUserPreferences = {"bio":"{{FAF:76365}}","privacy":{"profile":"public","games":"public","friends":"friends"}};
                                                window.profilesData.profileUserFriends = [];
                                                window.profilesData.currentUserFriends = [];
                                                window.profilesData.recentGames = [];
                                                            window.profilesData.activities = [];
                                                                                                            window.profilesData.recentAchievements = [];
                                                window.profilesData.profileUserFriendInvites = null;
                                            window.profilesData.serverNow = 1632687270;
                    window.profilesData.mode = "standard";
                    window.activeFeatures = {"menuMicroservice":true,"footerMicroservice":true,"new_footer_enabled":true};
        	</script>
        </body>
        </html>
        """);

    assertTrue(instance.verifyProfileToken(GOG_USERNAME, user, "{{FAF:76365}}"));
  }

  @Test
  void verifyGameOwnershipSuccess() {
    when(fafApiProperties.getGog().getGamesListUrl()).thenReturn(GAME_LIST_PAGE_URL);

    when(restTemplate.getForObject(String.format(GAME_LIST_PAGE_URL, GOG_USERNAME, 1), GogService.GogGamesListPage.class))
      .thenReturn(new GogService.GogGamesListPage(2, new GogService.GogGamesListEmbeddedList(List.of())));
    when(restTemplate.getForObject(String.format(GAME_LIST_PAGE_URL, GOG_USERNAME, 2), GogService.GogGamesListPage.class))
      .thenReturn(new GogService.GogGamesListPage(2, new GogService.GogGamesListEmbeddedList(
        List.of(
          new GogService.GogGamesListEntry(new GogService.GogGamesListEntryGameDetails("12345")),
          new GogService.GogGamesListEntry(new GogService.GogGamesListEntryGameDetails(GOG_FA_GAME_ID))
        )
      )));

    assertTrue(instance.verifyGameOwnership(GOG_USERNAME));
  }

  @Test
  void verifyGameOwnershipGameNotPresent() {
    when(fafApiProperties.getGog().getGamesListUrl()).thenReturn(GAME_LIST_PAGE_URL);

    when(restTemplate.getForObject(String.format(GAME_LIST_PAGE_URL, GOG_USERNAME, 1), GogService.GogGamesListPage.class))
      .thenReturn(new GogService.GogGamesListPage(1, new GogService.GogGamesListEmbeddedList(List.of())));

    assertFalse(instance.verifyGameOwnership(GOG_USERNAME));
  }

  @Test
  void verifyGameOwnershipGameNotPublic() {
    when(fafApiProperties.getGog().getGamesListUrl()).thenReturn(GAME_LIST_PAGE_URL);

    when(restTemplate.getForObject(String.format(GAME_LIST_PAGE_URL, GOG_USERNAME, 1), GogService.GogGamesListPage.class))
      .thenThrow(HttpClientErrorException.create(HttpStatus.FORBIDDEN, "", HttpHeaders.EMPTY, null, null));

    ApiException result = assertThrows(ApiException.class, () -> instance.verifyGameOwnership(GOG_USERNAME));
    assertThat(result, hasErrorCode(ErrorCode.GOG_LINK_GAMES_NOT_PUBLIC));
  }

  @Test
  void verifyGameOwnershipGameNotFound() {
    when(fafApiProperties.getGog().getGamesListUrl()).thenReturn(GAME_LIST_PAGE_URL);

    when(restTemplate.getForObject(String.format(GAME_LIST_PAGE_URL, GOG_USERNAME, 1), GogService.GogGamesListPage.class))
      .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "", HttpHeaders.EMPTY, null, null));

    ApiException result = assertThrows(ApiException.class, () -> instance.verifyGameOwnership(GOG_USERNAME));
    assertThat(result, hasErrorCode(ErrorCode.GOG_LINK_USER_NOT_FOUND));
  }
}
