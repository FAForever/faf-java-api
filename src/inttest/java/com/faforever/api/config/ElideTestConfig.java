package com.faforever.api.config;

import com.faforever.api.data.checks.IsAuthenticated;
import com.faforever.api.data.checks.IsClanLeader;
import com.faforever.api.data.checks.IsClanMembershipDeletable;
import com.faforever.api.data.checks.IsLoginOwner;
import com.faforever.api.data.checks.IsReviewOwner;
import com.faforever.api.data.checks.permission.HasBanRead;
import com.faforever.api.data.checks.permission.HasBanUpdate;
import com.faforever.api.data.checks.permission.HasLadder1v1Update;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yahoo.elide.Elide;
import com.yahoo.elide.ElideSettingsBuilder;
import com.yahoo.elide.audit.Slf4jLogger;
import com.yahoo.elide.core.EntityDictionary;
import com.yahoo.elide.core.filter.dialect.RSQLFilterDialect;
import com.yahoo.elide.datastores.hibernate5.HibernateStore;
import com.yahoo.elide.datastores.hibernate5.HibernateStore.Builder;
import com.yahoo.elide.jsonapi.JsonApiMapper;
import com.yahoo.elide.security.checks.Check;
import com.yahoo.elide.utils.coerce.CoerceUtil;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.hibernate.jpa.HibernateEntityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.persistence.EntityManager;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@Primary
public class ElideTestConfig {

  public static final String DEFAULT_CACHE_NAME = "Elide.defaultCache";

  @Bean
  public Elide elide(EntityManager entityManager, ObjectMapper objectMapper, EntityDictionary entityDictionary) {
    RSQLFilterDialect rsqlFilterDialect = new RSQLFilterDialect(entityDictionary);

    HibernateStore hibernateStore = new Builder((HibernateEntityManager) entityManager).build();

    registerAdditionalConverters();

    return new Elide(new ElideSettingsBuilder(hibernateStore)
      .withJsonApiMapper(new JsonApiMapper(entityDictionary, objectMapper))
      .withAuditLogger(new Slf4jLogger())
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
  public EntityDictionary entityDictionary() {
    ConcurrentHashMap<String, Class<? extends Check>> checks = new ConcurrentHashMap<>();
    checks.put(IsAuthenticated.EXPRESSION, IsAuthenticated.Inline.class);
    checks.put(IsLoginOwner.EXPRESSION, IsLoginOwner.Inline.class);
    checks.put(IsReviewOwner.EXPRESSION, IsReviewOwner.Inline.class);
    checks.put(IsClanLeader.EXPRESSION, IsClanLeader.Inline.class);
    checks.put(IsClanMembershipDeletable.EXPRESSION, IsClanMembershipDeletable.Inline.class);
    checks.put(HasBanRead.EXPRESSION, HasBanRead.Inline.class);
    checks.put(HasBanUpdate.EXPRESSION, HasBanUpdate.Inline.class);
    checks.put(HasLadder1v1Update.EXPRESSION, HasLadder1v1Update.Inline.class);

    return new EntityDictionary(checks);
  }
}