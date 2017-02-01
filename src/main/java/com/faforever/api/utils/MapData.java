package com.faforever.api.utils;

import lombok.Data;
import org.luaj.vm2.LuaTable;

// TODO: move to shared faf code
@Data
public class MapData {

  private byte[] ddsData;
  private LuaTable markers;
  private float width;
  private float height;
}
