package com.faforever.api.map;

import com.faforever.commons.lua.LuaAccessor;
import org.luaj.vm2.LuaValue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.OptionalInt;

public class MapLuaAccessor {
  private static final String ROOT_ELEMENT = "ScenarioInfo";
  private static final String CONFIGURATIONS = "Configurations";
  private static final String NAME = "name";
  private static final String DESCRIPTION = "description";
  private static final String TYPE = "type";
  private static final String SIZE = "size";
  private static final String NO_RUSH_RADIUS = "norushradius";
  private static final String MAP_VERSION = "map_version";
  private static final String CONFIGURATION_STANDARD = "standard";
  private static final String CONFIGURATION_STANDARD_TEAMS = "teams";
  private static final String CONFIGURATION_STANDARD_TEAMS_NAME = "name";
  private static final String CONFIGURATION_STANDARD_TEAMS_ARMIES = "armies";
  private static final String ADAPTIVE_MAP = "AdaptiveMap";

  private final LuaAccessor luaAccessor;

  private MapLuaAccessor(LuaAccessor luaAccessor) {
    this.luaAccessor = luaAccessor;
  }

  public static MapLuaAccessor of(Path scenarioLuaPath) throws IOException {
    return new MapLuaAccessor(LuaAccessor.of(scenarioLuaPath, ROOT_ELEMENT));
  }

  public static MapLuaAccessor of(String scenarioLuaCode) throws IOException {
    return new MapLuaAccessor(LuaAccessor.of(scenarioLuaCode, ROOT_ELEMENT));
  }

  public Optional<String> getName() {
    return luaAccessor.readVariableString(NAME);
  }

  public Optional<String> getDescription() {
    return luaAccessor.readVariableString(DESCRIPTION);
  }

  public Optional<String> getType() {
    return luaAccessor.readVariableString(TYPE);
  }

  public Optional<LuaValue> getSize() {
    return luaAccessor.readVariable(SIZE);
  }

  public OptionalInt getMapVersion() {
    return luaAccessor.readVariableInt(MAP_VERSION);
  }

  public OptionalInt getNoRushRadius() {
    return luaAccessor.readVariableInt(NO_RUSH_RADIUS);
  }

  public Optional<Boolean> isAdaptive() {
    return luaAccessor.readVariableBool(ADAPTIVE_MAP);
  }

  public boolean hasVariableMatchingIgnoreCase(String regex, String... names) {
    return luaAccessor.hasVariableMatchingIgnoreCase(regex, names);
  }

  public Optional<LuaValue> getFirstTeam() {
    Optional<LuaValue> configurationStandardTeamsOptional = luaAccessor.readVariable(
      CONFIGURATIONS, CONFIGURATION_STANDARD, CONFIGURATION_STANDARD_TEAMS);

    return configurationStandardTeamsOptional
      .map(teams -> teams.get(1));
  }

  public boolean hasInvalidTeam() {
    return getFirstTeam()
      .map(firstTeam ->
        !LuaAccessor.isValue(firstTeam, CONFIGURATION_STANDARD_TEAMS_NAME) ||
          !LuaAccessor.isValue(firstTeam, CONFIGURATION_STANDARD_TEAMS_ARMIES) ||
          !firstTeam.get(CONFIGURATION_STANDARD_TEAMS_NAME).tojstring().equals("FFA"))
      .orElse(true);
  }

  public String getName$() {
    return getName().get();
  }

  public String getDescription$() {
    return getDescription().get();
  }

  public String getType$() {
    return getType().get();
  }

  public LuaValue getSize$() {
    return getSize().get();
  }

  public int getMapVersion$() {
    return getMapVersion().getAsInt();
  }

  public LuaValue getFirstTeam$() {
    return getFirstTeam().get();
  }

  public boolean isAdaptive$() {
    return isAdaptive().get();
  }
}
