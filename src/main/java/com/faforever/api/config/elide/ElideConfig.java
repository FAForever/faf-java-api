package com.faforever.api.config.elide;

import com.faforever.api.security.ExtendedAuditLogger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yahoo.elide.Elide;
import com.yahoo.elide.ElideSettingsBuilder;
import com.yahoo.elide.core.datastore.DataStore;
import com.yahoo.elide.core.dictionary.EntityDictionary;
import com.yahoo.elide.core.dictionary.Injector;
import com.yahoo.elide.core.filter.dialect.CaseSensitivityStrategy;
import com.yahoo.elide.core.filter.dialect.RSQLFilterDialect;
import com.yahoo.elide.core.utils.coerce.CoerceUtil;
import com.yahoo.elide.datastores.multiplex.MultiplexManager;
import com.yahoo.elide.jsonapi.JsonApiMapper;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Collections;

@Configuration
public class ElideConfig {

  public static final String DEFAULT_CACHE_NAME = "Elide.defaultCache";

  @Bean
  MultiplexManager multiplexDataStore(
    DataStore fafDataStore,
    DataStore leagueDataStore
  ) {
    return new MultiplexManager(fafDataStore, leagueDataStore);
  }

  @Bean
  public Elide elide(DataStore multiplexDataStore, ObjectMapper objectMapper, EntityDictionary entityDictionary, ExtendedAuditLogger extendedAuditLogger) {
    RSQLFilterDialect rsqlFilterDialect = new RSQLFilterDialect(entityDictionary, new CaseSensitivityStrategy.UseColumnCollation());

    registerAdditionalConverters();

    return new Elide(new ElideSettingsBuilder(multiplexDataStore)
      .withJsonApiMapper(new JsonApiMapper(objectMapper))
      .withAuditLogger(extendedAuditLogger)
      .withEntityDictionary(entityDictionary)
      .withJoinFilterDialect(rsqlFilterDialect)
      .withSubqueryFilterDialect(rsqlFilterDialect)
      .build());
  }

  /**
   * See https://github.com/yahoo/elide/issues/428.
   */
  private void registerAdditionalConverters() {
    CoerceUtil.coerce("", String.class);
    ConvertUtils.register(new Converter() {
      @Override
      @SuppressWarnings("unchecked")
      public <T> T convert(Class<T> type, Object value) {
        return (T) OffsetDateTime.parse(String.valueOf(value));
      }
    }, OffsetDateTime.class);
    ConvertUtils.register(new Converter() {
      @Override
      @SuppressWarnings("unchecked")
      public <T> T convert(Class<T> type, Object value) {
        return (T) Instant.parse(String.valueOf(value));
      }
    }, Instant.class);
    ConvertUtils.register(new Converter() {
      @Override
      @SuppressWarnings("unchecked")
      public <T> T convert(Class<T> type, Object value) {
        return (T) Duration.parse(String.valueOf(value));
      }
    }, Duration.class);
  }

  @Bean
  public EntityDictionary entityDictionary(AutowireCapableBeanFactory beanFactory) {
    final EntityDictionary entityDictionary = new EntityDictionary(Collections.emptyMap(), new Injector() {
      @Override
      public void inject(Object entity) {
        beanFactory.autowireBean(entity);
      }

      @Override
      public <T> T instantiate(Class<T> cls) {
        return beanFactory.createBean(cls);
      }
    });

    entityDictionary.scanForSecurityChecks();

    return entityDictionary;
  }
}
