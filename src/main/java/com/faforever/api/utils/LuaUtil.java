package com.faforever.api.utils;

import com.google.common.io.CharStreams;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.charset.StandardCharsets.UTF_8;

// TODO: move to shared faf code
public final class LuaUtil {

  private LuaUtil() {
    throw new AssertionError("Not instantiatable");
  }

  public static LuaValue loadFile(Path file) throws IOException {
    try (InputStream inputStream = Files.newInputStream(file)) {
      return load(inputStream);
    }
  }

  public static LuaValue load(InputStream inputStream) throws IOException {
    Globals globals = JsePlatform.standardGlobals();
    try (InputStreamReader inputStreamReader = new InputStreamReader(LuaUtil.class.getResourceAsStream("/lua/faf.lua"), UTF_8)) {
      globals.baselib.load(globals.load(CharStreams.toString(inputStreamReader)));
      globals.load(inputStream, "@" + inputStream.hashCode(), "bt", globals).invoke();
      return globals;
    }
  }
}
