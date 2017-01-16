package com.faforever.api.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

public interface StorageService {

  void init();

  void store(MultipartFile file, Category category);

  Path load(String filename, Category category);

  Resource loadAsResource(String filename, Category category);

  void deleteAll();

  enum Category {
    MAP, MOD
  }
}
