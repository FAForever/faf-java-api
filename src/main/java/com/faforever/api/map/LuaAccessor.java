package com.faforever.api.map;

import com.faforever.commons.lua.LuaLoader;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.regex.PatternSyntaxException;

import static java.text.MessageFormat.format;

public class LuaAccessor {
  private final LuaValue rootContext;

  private LuaAccessor(LuaValue rootContext) {
    this.rootContext = rootContext;
  }

  static LuaAccessor of(Path luaPath, String rootElement) throws IOException, LuaException {
    LuaValue rootContext = LuaLoader.loadFile(luaPath);

    if (!isValue(rootContext, rootElement)) {
      throw new LuaException(format("Root element ''{0}'' is not defined."));
    }

    return new LuaAccessor(rootContext.get(rootElement));
  }

  static LuaAccessor of(String luaCode, String rootElement) throws IOException, LuaException {
    LuaValue rootContext = LuaLoader.load(luaCode);

    if (!isValue(rootContext, rootElement)) {
      throw new LuaException(format("Root element ''{0}'' is not defined."));
    }

    return new LuaAccessor(rootContext.get(rootElement));
  }

  public static boolean isValue(LuaValue parent, String name) {
    LuaValue value = parent.get(name);
    return isValue(value);
  }

  public static boolean isValue(LuaValue value) {
    return value != LuaValue.NIL && !(value instanceof LuaFunction);
  }

  Optional<LuaValue> readVariable(LuaValue parent, String... names) {
    if (names.length < 1) {
      throw new IllegalArgumentException("At least one variable name must be given!");
    }

    if (!isValue(parent, names[0])) {
      return Optional.empty();
    }

    LuaValue nextElement = parent.get(names[0]);

    if (names.length == 1) {
      return Optional.of(nextElement);
    } else {
      return readVariable(nextElement, Arrays.copyOfRange(names, 1, names.length));
    }
  }

  Optional<LuaValue> readVariable(String... names) {
    return readVariable(rootContext, names);
  }

  Optional<String> readVariableString(String... names) {
    return readVariable(rootContext, names)
      .map(LuaValue::tojstring);
  }

  public OptionalInt readVariableInt(String... names) {
    return readVariable(rootContext, names)
      .map(value -> OptionalInt.of(value.toint()))
      .orElseGet(OptionalInt::empty);
  }

  public boolean hasVariableMatching(String regex, String... names) {
    return readVariableString(names)
      .map(string -> {
        try {
          return string.matches(regex);
        } catch (PatternSyntaxException e) {
          return false;
        }
      })
      .orElse(false);
  }
}
