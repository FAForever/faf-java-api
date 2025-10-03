package com.faforever.api.db;

import com.faforever.api.config.FafApiProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.Ordered;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SchemaVersionVerifierTest {

  @Mock
  private SchemaVersionRepository schemaVersionRepository;

  @Mock
  private FafApiProperties properties;

  @Mock
  private FafApiProperties.Database databaseProperties;

  private SchemaVersionVerifier underTest;

  @BeforeEach
  void setUp() {
    lenient().when(properties.getDatabase()).thenReturn(databaseProperties);
    underTest = new SchemaVersionVerifier(schemaVersionRepository, properties);
  }

  /**
   * getOrder() should return the highest precedence constant so that this verifier runs before
   * other beans.
   */
  @Test
  void testOrderIsHighestPrecedence() {
    assertEquals(Ordered.HIGHEST_PRECEDENCE, underTest.getOrder());
  }

  /**
   * afterPropertiesSet() should complete normally when the actual DB version is greater than or
   * equal to the required version.
   */
  @Test
  void testNoExceptionIfVersionSufficient() {
    int minimumRequiredVersion = 3;
    String actualVersion = "5";

    when(databaseProperties.getSchemaVersion()).thenReturn(minimumRequiredVersion);
    when(schemaVersionRepository.findMaxVersion()).thenReturn(Optional.of(actualVersion));

    assertDoesNotThrow(() -> underTest.afterPropertiesSet());
  }

  /**
   * afterPropertiesSet() should throw IllegalStateException if no version string is present in the
   * repository.
   */
  @Test
  void testExceptionIfNoVersionFound() {
    int minimumRequiredVersion = 1;
    String actualVersion = null;

    when(databaseProperties.getSchemaVersion()).thenReturn(minimumRequiredVersion);
    when(schemaVersionRepository.findMaxVersion()).thenReturn(Optional.ofNullable(actualVersion));

    Exception ex = assertThrows(IllegalStateException.class, () -> underTest.afterPropertiesSet());
    assertEquals("No database version is available", ex.getMessage());
  }

  /**
   * afterPropertiesSet() should throw NumberFormatException if the version string in the repository
   * isn’t a valid integer.
   */
  @Test
  void exceptionIfVersionNotNumeric() {
    int minimumRequiredVersion = 1;
    String actualVersion = "not-a-number";

    when(databaseProperties.getSchemaVersion()).thenReturn(minimumRequiredVersion);
    when(schemaVersionRepository.findMaxVersion()).thenReturn(Optional.of(actualVersion));

    Exception exception = assertThrows(NumberFormatException.class,
      () -> underTest.afterPropertiesSet());

    assertTrue(exception.getMessage().contains(actualVersion));
  }

  /**
   * afterPropertiesSet() should throw IllegalStateException with an informative message when the
   * actual DB version is lower than required.
   */
  @Test
  void testExceptionIfVersionTooLow() {
    int minimumRequiredVersion = 10;
    String actualVersion = "7";

    when(databaseProperties.getSchemaVersion()).thenReturn(minimumRequiredVersion);
    when(schemaVersionRepository.findMaxVersion()).thenReturn(Optional.of(actualVersion));

    assertThrows(IllegalStateException.class, () -> underTest.afterPropertiesSet());
  }
}
