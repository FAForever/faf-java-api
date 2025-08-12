package com.faforever.api.db;

import com.faforever.api.config.ApplicationProfile;
import com.faforever.api.config.FafApiProperties;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.PriorityOrdered;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
@Profile("!" + ApplicationProfile.INTEGRATION_TEST)
public class SchemaVersionVerifier implements PriorityOrdered, InitializingBean {

  private final SchemaVersionRepository schemaVersionRepository;
  private final FafApiProperties properties;

  public SchemaVersionVerifier(SchemaVersionRepository schemaVersionRepository, FafApiProperties properties) {
    this.schemaVersionRepository = schemaVersionRepository;
    this.properties = properties;
  }

  @Override
  public int getOrder() {
    return HIGHEST_PRECEDENCE;
  }

  @Override
  public void afterPropertiesSet() {
    int minimumRequiredVersion = properties.getDatabase().getSchemaVersion();
    int actualVersion = schemaVersionRepository.findMaxVersion().map(Integer::parseInt)
      .orElseThrow(() -> new IllegalStateException("No database version is available"));

    Assert.state(actualVersion >= minimumRequiredVersion,
      String.format("Database version is '%d' but this software requires at least '%d'. If you are sure that this version is " +
          "compatible, you can override the expected version by setting the environment variable DATABASE_SCHEMA_VERSION.",
        actualVersion, minimumRequiredVersion));
  }
}
