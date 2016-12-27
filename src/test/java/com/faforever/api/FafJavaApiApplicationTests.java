package com.faforever.api;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@Ignore("Fix DB connection on travis")
public class FafJavaApiApplicationTests {

	@Test
	public void contextLoads() {
	}

}
