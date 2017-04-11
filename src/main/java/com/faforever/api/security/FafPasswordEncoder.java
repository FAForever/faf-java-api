/*
 * Copyright 2011-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.faforever.api.security;

import lombok.SneakyThrows;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * A pretty insecure SHA-256 password encoder.
 */
public final class FafPasswordEncoder implements PasswordEncoder {

  public String encode(CharSequence rawPassword) {
    byte[] digest = digest(rawPassword);
    return new String(Hex.encode(digest));
  }

  public boolean matches(CharSequence rawPassword, String encodedPassword) {
    byte[] digested = decode(encodedPassword);
    return matches(digested, digest(rawPassword));
  }

  @SneakyThrows
  private byte[] digest(CharSequence rawPassword) {
    MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
    return messageDigest.digest(rawPassword.toString().getBytes(StandardCharsets.UTF_8));
  }

  private byte[] decode(CharSequence encodedPassword) {
    return Hex.decode(encodedPassword);
  }

  /**
   * Constant time comparison to prevent against timing attacks.
   */
  private boolean matches(byte[] expected, byte[] actual) {
    if (expected.length != actual.length) {
      return false;
    }

    int result = 0;
    for (int i = 0; i < expected.length; i++) {
      result |= expected[i] ^ actual[i];
    }
    return result == 0;
  }
}
