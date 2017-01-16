package com.faforever.api.utils;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import java.nio.file.Path;

import static com.github.nocatch.NoCatch.noCatch;
import static java.nio.file.Files.createDirectories;
import static javax.imageio.ImageIO.write;

public class JavaFxUtil {
  public static void writeImage(Image image, Path path, String format) {
    if (path.getParent() != null) {
      noCatch(() -> createDirectories(path.getParent()));
    }
    noCatch(() -> write(SwingFXUtils.fromFXImage(image, null), format, path.toFile()));
  }
}
