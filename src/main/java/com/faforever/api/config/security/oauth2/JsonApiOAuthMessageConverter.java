package com.faforever.api.config.security.oauth2;

import com.faforever.api.error.ErrorResponse;
import com.faforever.api.error.ErrorResult;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;

import java.io.IOException;


public class JsonApiOAuthMessageConverter extends MappingJackson2HttpMessageConverter {

  @Override
  protected void writeInternal(Object object, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
    super.writeInternal(transformObject(object), outputMessage);
  }

  protected Object transformObject(Object object) {
    ErrorResponse response = new ErrorResponse();
    if (object instanceof OAuth2Exception) {
      OAuth2Exception oAuth2Exception = (OAuth2Exception) object;

      final ErrorResult newError = new ErrorResult(
        String.valueOf(oAuth2Exception.getHttpErrorCode()),
        oAuth2Exception.getOAuth2ErrorCode(),
        oAuth2Exception.getMessage()
      );
      response.addError(newError);
      newError.setMeta(ErrorResult.createMeta(null, oAuth2Exception.getAdditionalInformation()).orElse(null));
    } else {
      response.addError(new ErrorResult(
        String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()),
        "Error",
        object.toString()
      ));
    }
    return response;
  }
}
