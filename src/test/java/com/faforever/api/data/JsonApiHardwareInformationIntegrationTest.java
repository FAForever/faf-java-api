package com.faforever.api.data;

import com.faforever.api.data.domain.Player;
import com.faforever.integration.TestDatabase;
import com.faforever.integration.factories.HardwareInformationFactory;
import com.faforever.integration.factories.PlayerFactory;
import com.faforever.integration.utils.MockMvcHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.inject.Inject;
import javax.servlet.Filter;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@Import(TestDatabase.class)
public class JsonApiHardwareInformationIntegrationTest {

  private MockMvc mvc;
  private WebApplicationContext context;
  private Filter springSecurityFilterChain;

  private TestDatabase database;


  @Inject
  public void init(TestDatabase database,
                   WebApplicationContext context,
                   Filter springSecurityFilterChain) {
    this.context = context;
    this.springSecurityFilterChain = springSecurityFilterChain;
    this.database = database;
  }

  @Before
  public void setUp() {
    mvc = MockMvcBuilders
      .webAppContextSetup(context)
      .addFilter(springSecurityFilterChain)
      .build();
    database.assertEmptyDatabase();
  }

  @After
  public void tearDown() {
    // TODO: This is needed, because Elide has some problems with @Transactional annotation #71
    database.tearDown();
  }


  @Test
  public void getHardwareInformation() throws Exception {
    List<Player> players = Arrays.asList(
      PlayerFactory.create("Alice", "1234", database),
      PlayerFactory.create("Bob", "1234", database),
      PlayerFactory.create("Charlie", "1234", database),
      PlayerFactory.create("Dave", "1234", database),
      PlayerFactory.create("Elice", "1234", database)
    );

    HardwareInformationFactory.builder()
      .hash("1-2-3-4")
      .players(players)
      .database(database)
      .build();

    assertEquals(1, database.getHardwareInformationRepository().count());
    MockMvcHelper.of(this.mvc).perform(
      get("/data/HardwareInformation/"))
      .andExpect(jsonPath("$.data", hasSize(1)))
      .andExpect(jsonPath("$.data[0].type", is("HardwareInformation")))
      .andExpect(jsonPath("$.data[0].relationships.players.data", hasSize(players.size())))
      .andExpect(status().isOk());
    assertEquals(1, database.getHardwareInformationRepository().count());
  }

  // TODO: implement more test if permission system is ready #81
}
