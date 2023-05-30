package com.faforever.api.config;

import com.faforever.api.map.MapUploadMetadata;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * This class is used to support having both @RequestParam and @RequestPart with same multipart name in one request handler.
 * When multipart request contains simple request param octet-stream, this class is used to ignore parsing
 * of byte stream to {@link MapUploadMetadata}.
 * See {@link com.faforever.api.map.MapsController#uploadMap(MultipartFile, String, MapUploadMetadata, Authentication)}
 */
public class IgnoreOctetStreamToObjectHttpMessageConverter extends AbstractHttpMessageConverter<byte[]> {


  public IgnoreOctetStreamToObjectHttpMessageConverter() {
    super(MediaType.APPLICATION_OCTET_STREAM, MediaType.ALL);
  }


  @Override
  public boolean supports(Class<?> clazz) {
    return MapUploadMetadata.class == clazz;
  }

  @Override
  public byte[] readInternal(Class<? extends byte[]> clazz, HttpInputMessage message) throws IOException {
    return null;
  }

  @Override
  protected Long getContentLength(byte[] bytes, @Nullable MediaType contentType) {
    return 0L;
  }

  @Override
  protected void writeInternal(byte[] bytes, HttpOutputMessage outputMessage) throws IOException {
    StreamUtils.copy(bytes, outputMessage.getBody());
  }
}
