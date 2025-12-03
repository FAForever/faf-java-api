package com.faforever.api.error;

import com.faforever.api.logging.RequestIdFilter;
import org.slf4j.MDC;
import org.springframework.boot.jackson.JacksonComponent;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

import java.text.MessageFormat;

@JacksonComponent
public class ErrorJsonSerializer extends ValueSerializer<Error> {

  @Override
  public void serialize(Error error, JsonGenerator gen, SerializationContext ctxt) throws JacksonException {
    ErrorCode errorCode = error.getErrorCode();

    gen.writeStartObject();
    gen.writeNumberProperty("code", errorCode.getCode());
    gen.writeStringProperty("requestId", MDC.get(RequestIdFilter.REQUEST_ID_KEY));
    gen.writeStringProperty("title", MessageFormat.format(errorCode.getTitle(), error.getArgs()));
    gen.writeStringProperty("detail", MessageFormat.format(errorCode.getDetail(), error.getArgs()));
    gen.writePOJOProperty("args", error.getArgs());
    gen.writeEndObject();
  }
}
