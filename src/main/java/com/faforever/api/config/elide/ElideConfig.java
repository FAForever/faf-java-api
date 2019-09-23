package com.faforever.api.config.elide;

import com.faforever.api.data.checks.BooleanChange;
import com.faforever.api.data.checks.IsAuthenticated;
import com.faforever.api.data.checks.IsClanMembershipDeletable;
import com.faforever.api.data.checks.IsEntityOwner;
import com.faforever.api.data.checks.IsInAwaitingState;
import com.faforever.api.data.checks.permission.IsModerator;
import com.faforever.api.security.ExtendedAuditLogger;
import com.faforever.api.security.elide.permission.AdminAccountBanCheck;
import com.faforever.api.security.elide.permission.AdminAccountNoteCheck;
import com.faforever.api.security.elide.permission.AdminModerationReportCheck;
import com.faforever.api.security.elide.permission.AdminVoteCheck;
import com.faforever.api.security.elide.permission.ReadAccountPrivateDetailsCheck;
import com.faforever.api.security.elide.permission.ReadTeamkillReportCheck;
import com.faforever.api.security.elide.permission.ReadUserGroupCheck;
import com.faforever.api.security.elide.permission.WriteAvatarCheck;
import com.faforever.api.security.elide.permission.WriteEmailDomainBanCheck;
import com.faforever.api.security.elide.permission.WriteMatchmakerMapCheck;
import com.faforever.api.security.elide.permission.WriteTutorialCheck;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yahoo.elide.Elide;
import com.yahoo.elide.ElideSettingsBuilder;
import com.yahoo.elide.core.EntityDictionary;
import com.yahoo.elide.core.filter.dialect.CaseSensitivityStrategy;
import com.yahoo.elide.core.filter.dialect.RSQLFilterDialect;
import com.yahoo.elide.jsonapi.JsonApiMapper;
import com.yahoo.elide.security.checks.Check;
import com.yahoo.elide.utils.coerce.CoerceUtil;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.hibernate.ScrollMode;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManager;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class ElideConfig {

  public static final String DEFAULT_CACHE_NAME = "Elide.defaultCache";

  @Bean
  public Elide elide(SpringHibernateDataStore springHibernateDataStore, ObjectMapper objectMapper, EntityDictionary entityDictionary, ExtendedAuditLogger extendedAuditLogger) {
    RSQLFilterDialect rsqlFilterDialect = new RSQLFilterDialect(entityDictionary, new CaseSensitivityStrategy.UseColumnCollation());

    registerAdditionalConverters();

    return new Elide(new ElideSettingsBuilder(springHibernateDataStore)
      .withJsonApiMapper(new JsonApiMapper(entityDictionary, objectMapper))
      .withAuditLogger(extendedAuditLogger)
      .withEntityDictionary(entityDictionary)
      .withJoinFilterDialect(rsqlFilterDialect)
      .withSubqueryFilterDialect(rsqlFilterDialect)
      .build());
  }

  @Bean
  SpringHibernateDataStore springHibernateDataStore(PlatformTransactionManager txManager,
                                                    AutowireCapableBeanFactory beanFactory,
                                                    EntityManager entityManager) {
    return new SpringHibernateDataStore(txManager, beanFactory, entityManager, false, true, ScrollMode.FORWARD_ONLY);
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
    checks.put(BooleanChange.TO_FALSE_EXPRESSION, BooleanChange.ToFalse.class);
    checks.put(BooleanChange.TO_TRUE_EXPRESSION, BooleanChange.ToTrue.class);
    checks.put(IsInAwaitingState.EXPRESSION, IsInAwaitingState.Inline.class);
    checks.put(AdminAccountBanCheck.EXPRESSION, AdminAccountBanCheck.class);
    checks.put(AdminAccountNoteCheck.EXPRESSION, AdminAccountNoteCheck.class);
    checks.put(AdminModerationReportCheck.EXPRESSION, AdminModerationReportCheck.class);
    checks.put(ReadAccountPrivateDetailsCheck.EXPRESSION, ReadAccountPrivateDetailsCheck.class);
    checks.put(ReadTeamkillReportCheck.EXPRESSION, ReadTeamkillReportCheck.class);
    checks.put(ReadUserGroupCheck.EXPRESSION, ReadUserGroupCheck.class);
    checks.put(AdminVoteCheck.EXPRESSION, AdminVoteCheck.class);
    checks.put(WriteMatchmakerMapCheck.EXPRESSION, WriteMatchmakerMapCheck.class);
    checks.put(WriteTutorialCheck.EXPRESSION, WriteTutorialCheck.class);
    checks.put(WriteEmailDomainBanCheck.EXPRESSION, WriteEmailDomainBanCheck.class);
    checks.put(WriteAvatarCheck.EXPRESSION, WriteAvatarCheck.class);

    return new EntityDictionary(checks);
  }
}
