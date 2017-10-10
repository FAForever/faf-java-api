package com.faforever.api.data;

import com.faforever.api.data.domain.NameRecord;
import com.faforever.api.data.domain.Player;
import com.faforever.integration.TestDatabase;
import com.faforever.integration.factories.NameRecordFactory;
import com.faforever.integration.factories.PlayerFactory;
import com.faforever.integration.utils.MockMvcHelper;
import lombok.SneakyThrows;
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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@Import(TestDatabase.class)
public class JsonApiNameIntegrationTest {

  private MockMvc mvc;
  private WebApplicationContext context;
  private Filter springSecurityFilterChain;

  private TestDatabase database;


  @Inject
  public void init(TestDatabase database, WebApplicationContext context, Filter springSecurityFilterChain) {
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
  @SneakyThrows
  public void getNames() {
    Player player = PlayerFactory.builder()
      .login("UseFactory")
      .database(database)
      .build();
    NameRecord entity = NameRecordFactory.builder().name("MyFirstName")
      .id(999)
      .player(player)
      .database(database)
      .build();

    assertEquals(1, database.getNameRepository().count());
    MockMvcHelper.of(this.mvc).perform(get("/data/nameRecord"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data", hasSize(1)))
      .andExpect(jsonPath("$.data[0].id", is(String.valueOf(999))))
      .andExpect(jsonPath("$.data[0].type", is("nameRecord")))
      .andExpect(jsonPath("$.data[0].attributes.name", is(entity.getName())))
      .andExpect(jsonPath("$.data[0].relationships.player.data.id",
        is(String.valueOf(player.getId()))))
      .andExpect(jsonPath("$.data[0].relationships.player.data.type", is("player")));
    assertEquals(1, database.getNameRepository().count());
  }

}
