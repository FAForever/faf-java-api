package com.faforever.api.config.elide;

import com.faforever.api.config.ApplicationProfile;
import com.faforever.api.data.checks.IsAuthenticated;
import com.faforever.api.data.checks.IsClanMembershipDeletable;
import com.faforever.api.data.checks.IsEntityOwner;
import com.faforever.api.data.checks.permission.HasBanRead;
import com.faforever.api.data.checks.permission.HasBanUpdate;
import com.faforever.api.data.checks.permission.HasLadder1v1Update;
import com.faforever.api.data.checks.permission.IsModerator;
import com.faforever.api.security.ExtendedAuditLogger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yahoo.elide.Elide;
import com.yahoo.elide.ElideSettingsBuilder;
import com.yahoo.elide.core.EntityDictionary;
import com.yahoo.elide.core.filter.dialect.RSQLFilterDialect;
import com.yahoo.elide.datastores.hibernate5.HibernateStore;
import com.yahoo.elide.datastores.hibernate5.HibernateStore.Builder;
import com.yahoo.elide.jsonapi.JsonApiMapper;
import com.yahoo.elide.security.checks.Check;
import com.yahoo.elide.utils.coerce.CoerceUtil;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.hibernate.SessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.persistence.EntityManagerFactory;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class ElideConfig {

  public static final String DEFAULT_CACHE_NAME = "Elide.defaultCache";

  @Bean
  public Elide elide(HibernateStore hibernateStore, ObjectMapper objectMapper, EntityDictionary entityDictionary) {
    RSQLFilterDialect rsqlFilterDialect = new RSQLFilterDialect(entityDictionary);

    registerAdditionalConverters();

    return new Elide(new ElideSettingsBuilder(hibernateStore)
      .withJsonApiMapper(new JsonApiMapper(entityDictionary, objectMapper))
      .withAuditLogger(new ExtendedAuditLogger())
      .withEntityDictionary(entityDictionary)
      .withJoinFilterDialect(rsqlFilterDialect)
      .withSubqueryFilterDialect(rsqlFilterDialect)
      .build());
  }

  @Bean
  @Profile("!" + ApplicationProfile.INTEGRATION_TEST)
  HibernateStore hibernateStore(EntityManagerFactory entityManagerFactory) {
    return new Builder(entityManagerFactory.unwrap(SessionFactory.class)).build();
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
  public EntityDictionary entityDictionary() {
    ConcurrentHashMap<String, Class<? extends Check>> checks = new ConcurrentHashMap<>();
    checks.put(IsAuthenticated.EXPRESSION, IsAuthenticated.Inline.class);
    checks.put(IsModerator.EXPRESSION, IsModerator.Inline.class);
    checks.put(IsEntityOwner.EXPRESSION, IsEntityOwner.Inline.class);
    checks.put(IsClanMembershipDeletable.EXPRESSION, IsClanMembershipDeletable.Inline.class);
    checks.put(HasBanRead.EXPRESSION, HasBanRead.Inline.class);
    checks.put(HasBanUpdate.EXPRESSION, HasBanUpdate.Inline.class);
    checks.put(HasLadder1v1Update.EXPRESSION, HasLadder1v1Update.Inline.class);

    return new EntityDictionary(checks);
  }
}
