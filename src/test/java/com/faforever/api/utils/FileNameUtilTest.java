package com.faforever.api.utils;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class FileNameUtilTest {
  @Test
  public void generateFileName() throws Exception {
    final String fileName = FileNameUtil.normalizeFileName(".../.././////\\\\ :*?<>|\"{}:[]lala");
    assertThat(fileName, is("{}_[]lala"));
  }
}
