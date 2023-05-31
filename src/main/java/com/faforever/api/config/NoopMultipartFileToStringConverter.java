package com.faforever.api.config;

import com.faforever.api.map.MapUploadMetadata;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

/**
 * This class is used to support having both @RequestParam and @RequestPart with same multipart name in one request handler.
 * When multipart request contains json multipart, this class is used to ignore conversion
 * of MultipartFile to String.
 * See {@link com.faforever.api.map.MapsController#uploadMap(MultipartFile, String, MapUploadMetadata, Authentication)}
 */
public class NoopMultipartFileToStringConverter implements Converter<MultipartFile, String> {

  @Override
  public String convert(MultipartFile source) {
    return null;
  }

  @Override
  public <U> Converter<MultipartFile, U> andThen(Converter<? super String, ? extends U> after) {
    return Converter.super.andThen(after);
  }
}
