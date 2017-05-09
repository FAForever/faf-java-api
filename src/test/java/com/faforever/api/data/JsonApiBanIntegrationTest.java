package com.faforever.api.data;

import com.faforever.integration.TestDatabase;
import com.faforever.integration.factories.BanFactory;
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

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@Import(TestDatabase.class)
public class JsonApiBanIntegrationTest {

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
  public void getBansWithoutToken() throws Exception {
    BanFactory.builder().database(database).build();
    assertEquals(1, database.getBanRepository().count());
    assertEquals(2, database.getPlayerRepository().count());

    MockMvcHelper.of(this.mvc).perform(
      get("/data/banInfo"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data", hasSize(0)));

    assertEquals(1, database.getBanRepository().count());
    assertEquals(2, database.getPlayerRepository().count());
  }

  // TODO: implement more tests if permission system is ready #81
}
