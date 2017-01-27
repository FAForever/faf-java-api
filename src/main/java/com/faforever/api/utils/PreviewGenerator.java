package com.faforever.api.utils;

import com.google.common.io.LittleEndianDataInputStream;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;
import lombok.SneakyThrows;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

import javax.imageio.ImageIO;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

import static com.github.nocatch.NoCatch.noCatch;
import static java.awt.Image.SCALE_SMOOTH;
import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static java.nio.file.Files.list;

// TODO: move to shared faf code
public final class PreviewGenerator {

  private static final double RESOURCE_ICON_RATIO = 20 / 1024d;
  private static final String MASS_IMAGE = "/images/map_markers/mass.png";
  private static final String HYDRO_IMAGE = "/images/map_markers/hydro.png";
  private static final String ARMY_IMAGE = "/images/map_markers/army.png";
  private static final String ARMY_PREFIX = "ARMY_";

  private PreviewGenerator() {
    throw new AssertionError("Not instantiatable");
  }

  public static javafx.scene.image.Image generatePreview(Path mapFolder, int width, int height) throws IOException {
    try (Stream<Path> mapFolderStream = list(mapFolder)) {
      Optional<Path> mapPath = mapFolderStream
          .filter(file -> file.getFileName().toString().endsWith(".scmap"))
          .findFirst();
      if (!mapPath.isPresent()) {
        throw new RuntimeException("No map file was found in: " + mapFolder.toAbsolutePath());
      }

      return noCatch(() -> {
        MapData mapData = parseMap(mapPath.get());
        if (mapData == null) {
          throw new RuntimeException("mapdata is null after parseMap from: " + mapPath.get());
        }

        BufferedImage previewImage = getDdsImage(mapData);
        previewImage = scale(previewImage, width, height);

        addMarkers(previewImage, mapData);

        return SwingFXUtils.toFXImage(previewImage, new WritableImage(width, height));
      });

    }
  }

  @SneakyThrows
  private static MapData parseMap(Path mapPath) {
    MapData mapData = new MapData();
    try (LittleEndianDataInputStream mapInput = new LittleEndianDataInputStream(Files.newInputStream(mapPath))) {
      mapInput.skip(16);
      mapData.setWidth((int) mapInput.readFloat());
      mapData.setHeight((int) mapInput.readFloat());
      mapInput.skip(6);

      int ddsSize = mapInput.readInt();
      // Skip DDS header
      mapInput.skipBytes(128);

      byte[] buffer = new byte[ddsSize - 128];
      mapInput.readFully(buffer);

      mapData.setDdsData(buffer);

      Path lua;
      try (Stream<Path> fileStream = list(mapPath.getParent())) {
        Optional<Path> saveLua = fileStream
            .filter(filePath -> filePath.toString().toLowerCase().endsWith("_save.lua"))
            .findFirst();
        lua = saveLua.get();
      }
      if (!Files.isRegularFile(lua)) {
        throw new RuntimeException("Path is no regular file: " + lua);
      }
      LuaTable markers = LuaUtil.loadFile(lua).get("Scenario").get("MasterChain").get("_MASTERCHAIN_").get("Markers").checktable();
      mapData.setMarkers(markers);
    }
    return mapData;
  }

  private static BufferedImage getDdsImage(MapData mapData) throws IOException {
    byte[] ddsData = mapData.getDdsData();
    int ddsDimension = (int) (Math.sqrt(ddsData.length) / 2);

    bgraToAbgr(ddsData);
    BufferedImage previewImage = new BufferedImage(ddsDimension, ddsDimension, BufferedImage.TYPE_4BYTE_ABGR);
    previewImage.setData(Raster.createRaster(previewImage.getSampleModel(), new DataBufferByte(ddsData, ddsData.length), new Point()));
    return previewImage;
  }

  private static BufferedImage scale(BufferedImage previewImage, double width, double height) {
    int targetWidth = width < 1 ? 1 : (int) width;
    int targetHeight = height < 1 ? 1 : (int) height;

    Image image = previewImage.getScaledInstance(targetWidth, targetHeight, SCALE_SMOOTH);
    BufferedImage scaledImage = new BufferedImage(targetWidth, targetHeight, TYPE_INT_ARGB);

    Graphics graphics = scaledImage.createGraphics();
    graphics.drawImage(image, 0, 0, null);
    graphics.dispose();

    return scaledImage;
  }

  private static void addMarkers(BufferedImage previewImage, MapData mapData) throws IOException {
    float width = previewImage.getWidth();
    float height = previewImage.getHeight();

    Image massImage = scale(readImage(MASS_IMAGE), RESOURCE_ICON_RATIO * width, RESOURCE_ICON_RATIO * height);
    Image hydroImage = scale(readImage(HYDRO_IMAGE), RESOURCE_ICON_RATIO * width, RESOURCE_ICON_RATIO * height);
    Image armyImage = scale(readImage(ARMY_IMAGE), RESOURCE_ICON_RATIO * width, RESOURCE_ICON_RATIO * height);

    LuaTable markers = mapData.getMarkers();
    for (LuaValue key : markers.keys()) {
      LuaTable markerData = markers.get(key).checktable();

      switch (markerData.get("type").toString()) {
        case "Mass":
          addMarker(massImage, mapData, markerData, previewImage);
          break;
        case "Hydrocarbon":
          addMarker(hydroImage, mapData, markerData, previewImage);
          break;
        case "Blank Marker":
          if (!key.tojstring().startsWith(ARMY_PREFIX)) {
            continue;
          }
          addMarker(armyImage, mapData, markerData, previewImage);
          break;
      }
    }
  }

  private static void bgraToAbgr(byte[] buffer) {
    for (int i = 0; i < buffer.length; i += 4) {
      byte a = buffer[i + 3];
      buffer[i + 3] = buffer[i + 2];
      buffer[i + 2] = buffer[i + 1];
      buffer[i + 1] = buffer[i];
      buffer[i] = a;
    }
  }

  private static BufferedImage readImage(String resource) throws IOException {
    try (InputStream inputStream = PreviewGenerator.class.getResourceAsStream(resource)) {
      return ImageIO.read(inputStream);
    }
  }

  private static void addMarker(Image source, MapData mapData, LuaTable markerData, BufferedImage target) throws IOException {
    LuaTable vector = markerData.get("position").checktable();
    float x = vector.get(1).tofloat() / mapData.getWidth();
    float y = vector.get(3).tofloat() / mapData.getHeight();

    paintOnImage(source, x, y, target);
  }

  private static void paintOnImage(Image overlay, float xPercent, float yPercent, BufferedImage baseImage) {
    int overlayWidth = overlay.getWidth(null);
    int overlayHeight = overlay.getHeight(null);
    int x = (int) (xPercent * baseImage.getWidth() - overlayWidth / 2);
    int y = (int) (yPercent * baseImage.getHeight() - overlayHeight / 2);

    x = Math.min(Math.max(0, x), baseImage.getWidth() - overlayWidth);
    y = Math.min(Math.max(0, y), baseImage.getHeight() - overlayHeight);

    baseImage.getGraphics().drawImage(overlay, x, y, null);
  }
}
