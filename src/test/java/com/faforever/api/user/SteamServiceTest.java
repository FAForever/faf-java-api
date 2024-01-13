package com.faforever.api.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.faforever.api.data.domain.AccountLink;
import com.faforever.api.data.domain.LinkedServiceType;
import com.faforever.api.data.domain.User;
import com.faforever.api.error.ApiException;
import com.faforever.api.error.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SteamServiceTest {

  private static final String IDENTITY_NAME_PARAM = "openid.identity";
  private static final String DUMMY_URL = "valid.url.domain/login/123";
  private static final String DUMMY_RESPONSE = "dummy response";
  @Mock
  private AccountLinkRepository accountLinkRepositoryMock;
  @InjectMocks
  private SteamService beanUnderTest;

  @Test
  void testHandleInvalidOpenIdRedirect() {
    HttpServletRequest requestMock = Mockito.mock(HttpServletRequest.class);
    when(requestMock.getParameterMap())
        .thenReturn(Map.of(IDENTITY_NAME_PARAM, new String[]{DUMMY_URL}));
    when(requestMock.getParameter(IDENTITY_NAME_PARAM)).thenReturn(DUMMY_URL);

    ApiException thrownException = assertThrows(ApiException.class,
        () -> beanUnderTest.handleInvalidOpenIdRedirect(requestMock, DUMMY_RESPONSE));
    assertEquals(ErrorCode.STEAM_LOGIN_VALIDATION_FAILED,
        thrownException.getErrors()[0].getErrorCode());
  }

  @Test
  void testHandleInvalidOpenIdRedirectBlankIdentityParam() {
    HttpServletRequest requestMock = Mockito.mock(HttpServletRequest.class);
    final String blankDummyUrl = "";
    when(requestMock.getParameterMap())
        .thenReturn(Map.of(IDENTITY_NAME_PARAM, new String[]{blankDummyUrl}));
    when(requestMock.getParameter(IDENTITY_NAME_PARAM)).thenReturn(blankDummyUrl);

    ApiException thrownException = assertThrows(ApiException.class,
        () -> beanUnderTest.handleInvalidOpenIdRedirect(requestMock, DUMMY_RESPONSE));
    assertEquals(ErrorCode.STEAM_LOGIN_VALIDATION_FAILED,
        thrownException.getErrors()[0].getErrorCode());
  }

  @Test
  void testHandleInvalidOpenIdRedirectNoIdentityInRequest() {
    HttpServletRequest requestMock = Mockito.mock(HttpServletRequest.class);
    when(requestMock.getParameterMap()).thenReturn(Collections.emptyMap());

    ApiException thrownException = assertThrows(ApiException.class,
        () -> beanUnderTest.handleInvalidOpenIdRedirect(requestMock, DUMMY_RESPONSE));
    assertEquals(ErrorCode.STEAM_LOGIN_VALIDATION_FAILED,
        thrownException.getErrors()[0].getErrorCode());
  }

  @Test
  void testHandleInvalidOpenIdRedirectLinkedAccountExists() {
    HttpServletRequest requestMock = Mockito.mock(HttpServletRequest.class);
    User userMock = Mockito.mock(User.class);
    when(userMock.getId()).thenReturn(1);
    when(userMock.getLogin()).thenReturn("dummyLogin");
    AccountLink accountLinkMock = Mockito.mock(AccountLink.class);
    when(accountLinkMock.getUser()).thenReturn(userMock);
    when(requestMock.getParameterMap())
        .thenReturn(Map.of(IDENTITY_NAME_PARAM, new String[]{DUMMY_URL}));
    when(requestMock.getParameter(IDENTITY_NAME_PARAM)).thenReturn(DUMMY_URL);
    when(accountLinkRepositoryMock.findOneByServiceIdAndServiceType(anyString(),
        any(LinkedServiceType.class))).thenReturn(
        Optional.of(accountLinkMock));

    ApiException thrownException = assertThrows(ApiException.class,
        () -> beanUnderTest.handleInvalidOpenIdRedirect(requestMock, DUMMY_RESPONSE));
    assertEquals(ErrorCode.STEAM_LOGIN_VALIDATION_FAILED,
        thrownException.getErrors()[0].getErrorCode());
  }

}
