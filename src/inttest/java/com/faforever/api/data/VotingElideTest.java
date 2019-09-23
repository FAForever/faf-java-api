package com.faforever.api.data;

import com.faforever.api.AbstractIntegrationTest;
import com.faforever.api.data.domain.GroupPermission;
import com.faforever.api.data.domain.VotingChoice;
import com.faforever.api.data.domain.VotingQuestion;
import com.faforever.api.security.OAuthScope;
import com.faforever.api.voting.VotingQuestionRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepDefaultUser.sql")
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepVotingData.sql")
@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sql/cleanVotingData.sql")
public class VotingElideTest extends AbstractIntegrationTest {
  /*
  {
    "data": {
        "type": "votingSubject",
        "id": "2",
        "attributes": {
            "revealWinner": true
        }
    }
  }
   */
  private static final String PATCH_VOTING_SUBJECT_REVEAL_ID_2 = "{\n" +
    "    \"data\": {\n" +
    "        \"type\": \"votingSubject\",\n" +
    "        \"id\": \"2\",\n" +
    "        \"attributes\": {\n" +
    "            \"revealWinner\": true\n" +
    "        }\n" +
    "    }\n" +
    "}";

  /*
  {"data":
    {"type":"votingSubject",
      "attributes":{
        "subjectKey":"bla",
        "numberOfVotes":0,
        "topicUrl":"test",
        "beginOfVoteTime":"2018-09-09T11:00:00Z",
        "endOfVoteTime":"2018-09-09T11:00:00Z",
        "minGamesToVote":0,
        "descriptionKey":"test",
        "revealWinner":false},
      "relationships":{
        "votingQuestions":{
          "data":[]
        }
      }
    }
  }
  */
  private static final String CREATE_VOTING_SUBJECT_REVEAL_WINNER_FALSE = "{\"data\":\n" +
    "    {\"type\":\"votingSubject\",\n" +
    "      \"attributes\":{\n" +
    "        \"subjectKey\":\"bla\",\n" +
    "        \"numberOfVotes\":0,\n" +
    "        \"topicUrl\":\"test\",\n" +
    "        \"beginOfVoteTime\":\"2018-09-09T11:00:00Z\",\n" +
    "        \"endOfVoteTime\":\"2018-09-09T11:00:00Z\",\n" +
    "        \"minGamesToVote\":0,\n" +
    "        \"descriptionKey\":\"test\",\n" +
    "        \"revealWinner\":false},\n" +
    "      \"relationships\":{\n" +
    "        \"votingQuestions\":{\n" +
    "          \"data\":[]\n" +
    "        }\n" +
    "      }\n" +
    "    }\n" +
    "  }";

  /*
{"data":
  {"type":"votingSubject",
    "attributes":{
      "subjectKey":"bla",
      "numberOfVotes":0,
      "topicUrl":"test",
      "beginOfVoteTime":"2018-09-09T11:00:00Z",
      "endOfVoteTime":"2018-09-09T11:00:00Z",
      "minGamesToVote":0,
      "descriptionKey":"test",
      "revealWinner":true},
    "relationships":{
      "votingQuestions":{
        "data":[]
      }
    }
  }
}
*/
  private static final String CREATE_VOTING_SUBJECT_REVEAL_WINNER_TRUE = "{\"data\":\n" +
    "    {\"type\":\"votingSubject\",\n" +
    "      \"attributes\":{\n" +
    "        \"subjectKey\":\"bla\",\n" +
    "        \"numberOfVotes\":0,\n" +
    "        \"topicUrl\":\"test\",\n" +
    "        \"beginOfVoteTime\":\"2018-09-09T11:00:00Z\",\n" +
    "        \"endOfVoteTime\":\"{end-time}\",\n" +
    "        \"minGamesToVote\":0,\n" +
    "        \"descriptionKey\":\"test\",\n" +
    "        \"revealWinner\":true},\n" +
    "      \"relationships\":{\n" +
    "        \"votingQuestions\":{\n" +
    "          \"data\":[]\n" +
    "        }\n" +
    "      }\n" +
    "    }\n" +
    "  }";

  /*
  {
    "data": {
        "type": "votingSubject",
        "id": "1",
        "attributes": {
            "revealWinner": true
        }
    }
  }
   */
  private static final String PATCH_VOTING_SUBJECT_REVEAL_ID_1 = "{\n" +
    "    \"data\": {\n" +
    "        \"type\": \"votingSubject\",\n" +
    "        \"id\": \"1\",\n" +
    "        \"attributes\": {\n" +
    "            \"revealWinner\": true\n" +
    "        }\n" +
    "    }\n" +
    "}";
  /*
  {

  	"votingSubject":{
  		"id":1
  	},
  	"votingAnswers":[
  		{
  			"votingQuestion":{
  				"id":1
  			},
  			"alternativeOrdinal":0,
  			"votingChoice":{
  				"id":1
  			}
  		}
  	]

}
   */
  private static final String POST_VOTE_SUBJECT1 =
    "{\n" +
      "\n" +
      "\"votingSubject\":{\n" +
      "\"id\":1\n" +
      "},\n" +
      "\"votingAnswers\":[\n" +
      "{\n" +
      "\"votingQuestion\":{\n" +
      "\"id\":1\n" +
      "},\n" +
      "\"alternativeOrdinal\":0,\n" +
      "\"votingChoice\":{\n" +
      "\"id\":1\n" +
      "}\n" +
      "}\n" +
      "]\n" +
      "\n" +
      "}";

  /*
  {

  	"votingSubject":{
  		"id":2
  	},
  	"votingAnswers":[
  		{
  			"votingQuestion":{
  				"id":2
  			},
  			"alternativeOrdinal":0,
  			"votingChoice":{
  				"id":3
  			}
  		}
  	]

  }
   */
  private static final String POST_VOTE_SUBJECT2 = "{\n" +
    "\n" +
    "  \t\"votingSubject\":{\n" +
    "  \t\t\"id\":2\n" +
    "  \t},\n" +
    "  \t\"votingAnswers\":[\n" +
    "  \t\t{\n" +
    "  \t\t\t\"votingQuestion\":{\n" +
    "  \t\t\t\t\"id\":2\n" +
    "  \t\t\t},\n" +
    "  \t\t\t\"alternativeOrdinal\":0,\n" +
    "  \t\t\t\"votingChoice\":{\n" +
    "  \t\t\t\t\"id\":3\n" +
    "  \t\t\t}\n" +
    "  \t\t}\n" +
    "  \t]\n" +
    "\n" +
    "}";

  @Autowired
  VotingQuestionRepository votingQuestionRepository;


  @Test
  public void noBodyCanSeeOtherPeoplesVote() throws Exception {
    mockMvc.perform(get("/data/vote")
      .with(getOAuthTokenWithTestUser(OAuthScope._VOTE, NO_AUTHORITIES)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data", hasSize(0)));
  }

  @Test
  public void everyBodySeesVotingSubjects() throws Exception {
    mockMvc.perform(get("/data/votingSubject")
      .with(getOAuthTokenWithTestUser(NO_SCOPE, NO_AUTHORITIES)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data", hasSize(2)));
  }

  @Test
  public void everyBodySeesVotingQuestions() throws Exception {
    mockMvc.perform(get("/data/votingQuestion")
      .with(getOAuthTokenWithTestUser(NO_SCOPE, NO_AUTHORITIES)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data", hasSize(2)));
  }

  @Test
  public void everyBodySeesVotingChoices() throws Exception {
    mockMvc.perform(get("/data/votingChoice")
      .with(getOAuthTokenWithTestUser(NO_SCOPE, NO_AUTHORITIES)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data", hasSize(3)));
  }

  @Test
  public void cannotRevealWinnerOnEndedSubjectWorksWithoutScope() throws Exception {
    mockMvc.perform(
      patch("/data/votingSubject/2")
        .with(getOAuthTokenWithTestUser(NO_SCOPE, GroupPermission.ROLE_ADMIN_VOTE))
        .header(HttpHeaders.CONTENT_TYPE, DataController.JSON_API_MEDIA_TYPE)
        .content(PATCH_VOTING_SUBJECT_REVEAL_ID_2))
      .andExpect(status().isForbidden());
  }

  @Test
  public void cannotRevealWinnerOnEndedSubjectWorksWithoutRole() throws Exception {
    mockMvc.perform(
      patch("/data/votingSubject/2")
        .with(getOAuthTokenWithTestUser(OAuthScope._ADMINISTRATIVE_ACTION, NO_AUTHORITIES))
        .header(HttpHeaders.CONTENT_TYPE, DataController.JSON_API_MEDIA_TYPE)
        .content(PATCH_VOTING_SUBJECT_REVEAL_ID_2))
      .andExpect(status().isForbidden());
  }

  @Test
  public void canRevealWinnerOnEndedSubjectWorksWithScopeAndRole() throws Exception {
    mockMvc.perform(
      patch("/data/votingSubject/2")
        .with(getOAuthTokenWithTestUser(OAuthScope._ADMINISTRATIVE_ACTION, GroupPermission.ROLE_ADMIN_VOTE))
        .header(HttpHeaders.CONTENT_TYPE, DataController.JSON_API_MEDIA_TYPE)
        .content(PATCH_VOTING_SUBJECT_REVEAL_ID_2))
      .andExpect(status().isNoContent());
    VotingQuestion question = votingQuestionRepository.getOne(2);
    List<VotingChoice> winners = question.getWinners();
    assertThat(winners, hasSize(1));
    assertThat(winners.get(0).getId(), is(3));
  }

  @Test
  public void cannotRevealWinnerOnNoneEndedSubjectFails() throws Exception {
    mockMvc.perform(
      patch("/data/votingSubject/1")
        .with(getOAuthTokenWithTestUser(OAuthScope._ADMINISTRATIVE_ACTION, GroupPermission.ROLE_ADMIN_VOTE))
        .header(HttpHeaders.CONTENT_TYPE, DataController.JSON_API_MEDIA_TYPE)
        .content(PATCH_VOTING_SUBJECT_REVEAL_ID_1))
      .andExpect(status().is4xxClientError());
  }

  @Test
  public void cannotPostVoteWithoutScope() throws Exception {
    mockMvc.perform(post("/voting/vote")
      .with(getOAuthTokenWithTestUser(NO_SCOPE, NO_AUTHORITIES))
      .contentType(MediaType.APPLICATION_JSON)
      .content(POST_VOTE_SUBJECT1))
      .andExpect(status().isForbidden());
  }

  @Test
  public void canPostVoteWithScope() throws Exception {
    mockMvc.perform(post("/voting/vote")
      .with(getOAuthTokenWithTestUser(OAuthScope._VOTE, NO_AUTHORITIES))
      .contentType(MediaType.APPLICATION_JSON)
      .content(POST_VOTE_SUBJECT1))
      .andExpect(status().isOk());
  }

  @Test
  public void cannotPostVoteWhereVoteAlreadyExists() throws Exception {
    canPostVoteWithScope();

    mockMvc.perform(post("/voting/vote")
      .with(getOAuthTokenWithTestUser(OAuthScope._VOTE, NO_AUTHORITIES))
      .contentType(MediaType.APPLICATION_JSON)
      .content(POST_VOTE_SUBJECT1))
      .andExpect(status().isUnprocessableEntity());
  }

  @Test
  public void cannotPostVoteOnEndedSubject() throws Exception {
    mockMvc.perform(post("/voting/vote")
      .with(getOAuthTokenWithTestUser(OAuthScope._VOTE, NO_AUTHORITIES))
      .contentType(MediaType.APPLICATION_JSON)
      .content(POST_VOTE_SUBJECT2))
      .andExpect(status().isUnprocessableEntity());
  }

  @Test
  public void cannotPostVotingSubjectWithoutScope() throws Exception {
    mockMvc.perform(post("/data/votingSubject")
      .contentType(MediaType.APPLICATION_JSON)
      .with(getOAuthTokenWithTestUser(NO_SCOPE, GroupPermission.ROLE_ADMIN_VOTE))
      .content(CREATE_VOTING_SUBJECT_REVEAL_WINNER_FALSE))
      .andExpect(status().isForbidden());
  }

  @Test
  public void cannotPostVotingSubjectWithoutRole() throws Exception {
    mockMvc.perform(post("/data/votingSubject")
      .contentType(MediaType.APPLICATION_JSON)
      .with(getOAuthTokenWithTestUser(OAuthScope._ADMINISTRATIVE_ACTION, NO_AUTHORITIES))
      .content(CREATE_VOTING_SUBJECT_REVEAL_WINNER_FALSE))
      .andExpect(status().isForbidden());
  }

  @Test
  public void canPostVotingSubjectWithScopeAndRole() throws Exception {
    mockMvc.perform(post("/data/votingSubject")
      .contentType(MediaType.APPLICATION_JSON)
      .with(getOAuthTokenWithTestUser(OAuthScope._ADMINISTRATIVE_ACTION, GroupPermission.ROLE_ADMIN_VOTE))
      .content(CREATE_VOTING_SUBJECT_REVEAL_WINNER_FALSE))
      .andExpect(status().isCreated());
  }

  @Test
  public void cannotPostVotingSubjectWithRevealWinnerTrueButVoteNotEnded() throws Exception {
    String votingSubject = CREATE_VOTING_SUBJECT_REVEAL_WINNER_TRUE.replaceAll("\\{end-time}", OffsetDateTime.now().plusYears(1).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
    mockMvc.perform(post("/data/votingSubject")
      .with(getOAuthTokenWithTestUser(OAuthScope._ADMINISTRATIVE_ACTION, GroupPermission.ROLE_ADMIN_VOTE))
      .contentType(MediaType.APPLICATION_JSON)
      .content(votingSubject))
      .andExpect(status().is(400));
  }
}
