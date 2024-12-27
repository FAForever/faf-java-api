package com.faforever.api.config.elide;

import com.faforever.api.security.ExtendedAuditLogger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yahoo.elide.Elide;
import com.yahoo.elide.ElideSettings;
import com.yahoo.elide.core.TransactionRegistry;
import com.yahoo.elide.core.datastore.DataStore;
import com.yahoo.elide.core.dictionary.EntityDictionary;
import com.yahoo.elide.core.dictionary.Injector;
import com.yahoo.elide.core.filter.dialect.CaseSensitivityStrategy;
import com.yahoo.elide.core.filter.dialect.RSQLFilterDialect;
import com.yahoo.elide.core.utils.DefaultClassScanner;
import com.yahoo.elide.core.utils.coerce.CoerceUtil;
import com.yahoo.elide.datastores.multiplex.MultiplexManager;
import com.yahoo.elide.jsonapi.JsonApi;
import com.yahoo.elide.jsonapi.JsonApiMapper;
import com.yahoo.elide.jsonapi.JsonApiSettings;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Set;

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
    RSQLFilterDialect rsqlFilterDialect = new RSQLFilterDialect(entityDictionary, new CaseSensitivityStrategy.UseColumnCollation(), true);

    registerAdditionalConverters();

    final ElideSettings elideSettings = ElideSettings.builder()
      .dataStore(multiplexDataStore)
      .settings(JsonApiSettings.JsonApiSettingsBuilder.withDefaults(entityDictionary)
        .jsonApiMapper(new JsonApiMapper(objectMapper))
        .joinFilterDialect(rsqlFilterDialect)
        .subqueryFilterDialect(rsqlFilterDialect)
      )
      .auditLogger(extendedAuditLogger)
      .entityDictionary(entityDictionary)
      .build();
    return new Elide(elideSettings, new TransactionRegistry(), elideSettings.getEntityDictionary().getScanner(), true);
  }

  @Bean
  public JsonApi jsonApi(Elide elide) {
    return new JsonApi(elide);
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
    final EntityDictionary entityDictionary = new EntityDictionary(Map.of(), Map.of(), new Injector() {
      @Override
      public void inject(Object entity) {
        beanFactory.autowireBean(entity);
      }

      @Override
      public <T> T instantiate(Class<T> cls) {
        return beanFactory.createBean(cls);
      }
    },
      CoerceUtil::lookup,
      Set.of(),
      new DefaultClassScanner(),
      null);

    entityDictionary.scanForSecurityChecks();

    return entityDictionary;
  }
}
