package com.faforever.api.utils;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class NameUtilTest {
  @Test
  public void testIsPrintableAsciiString() {
    final boolean positiveResult = NameUtil.isPrintableAsciiString(" !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~");
    assertThat(positiveResult, is(true));

    final boolean negativeResult = NameUtil.isPrintableAsciiString("Hello this is ä test.");
    assertThat(negativeResult, is(false));
  }

  @Test
  public void generateFileName() throws Exception {
    final String fileName = NameUtil.normalizeFileName("a :*?<>|\"{}:[]la/ \\la??0");
    assertThat(fileName, is("a_____________la___la__0"));
  }
}
