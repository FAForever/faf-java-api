package com.faforever.api.error;

import com.faforever.api.logging.RequestIdFilter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.slf4j.MDC;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;
import java.text.MessageFormat;

@JsonComponent
public class ErrorJsonSerializer extends JsonSerializer<Error> {
  @Override
  public void serialize(Error error, JsonGenerator gen, SerializerProvider serializers) throws IOException {
    ErrorCode errorCode = error.getErrorCode();

    gen.writeStartObject();
    gen.writeNumberField("code", errorCode.getCode());
    gen.writeStringField("requestId", MDC.get(RequestIdFilter.REQUEST_ID_KEY));
    gen.writeStringField("title", MessageFormat.format(errorCode.getTitle(), error.getArgs()));
    gen.writeStringField("detail", MessageFormat.format(errorCode.getDetail(), error.getArgs()));
    gen.writeObjectField("args", error.getArgs());
    gen.writeEndObject();
  }
}
