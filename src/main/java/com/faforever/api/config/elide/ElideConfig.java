package com.faforever.api.config.elide;

import com.faforever.api.data.DataController;
import com.faforever.api.data.checks.IsAuthenticated;
import com.faforever.api.data.checks.IsClanLeader;
import com.faforever.api.data.checks.IsClanMembershipDeletable;
import com.faforever.api.data.checks.IsLoginOwner;
import com.faforever.api.data.checks.IsReviewOwner;
import com.faforever.api.data.checks.permission.HasBanRead;
import com.faforever.api.data.checks.permission.HasBanUpdate;
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
import org.hibernate.SessionFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.AbstractCacheResolver;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerMapping;

import javax.persistence.EntityManagerFactory;
import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class ElideConfig {

  @Bean
  public Elide elide(EntityManagerFactory entityManagerFactory, ObjectMapper objectMapper, EntityDictionary entityDictionary) {
    RSQLFilterDialect rsqlFilterDialect = new RSQLFilterDialect(entityDictionary);

    HibernateStore hibernateStore = new Builder(entityManagerFactory.unwrap(SessionFactory.class)).build();

    return new Elide(new ElideSettingsBuilder(hibernateStore)
        .withJsonApiMapper(new JsonApiMapper(entityDictionary, objectMapper))
        .withAuditLogger(new Slf4jLogger())
        .withEntityDictionary(entityDictionary)
        .withJoinFilterDialect(rsqlFilterDialect)
        .withSubqueryFilterDialect(rsqlFilterDialect)
        .build());
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

    return new EntityDictionary(checks);
  }

  /**
   * Returns a cache resolver that resolves cache names by JSON API type names. For instance, the type "map" will be
   * resolved to a cache named "map".
   */
  @Bean
  public CacheResolver elideCacheResolver(CacheManager cacheManager) {
    return new AbstractCacheResolver(cacheManager) {
      @Override
      protected Collection<String> getCacheNames(CacheOperationInvocationContext<?> context) {
        String jsonApiPath = getDataApiPath((HttpServletRequest) context.getArgs()[1]);
        String type = jsonApiPath.split("/")[0];

        if (!cacheManager.getCacheNames().contains(type)) {
          return Collections.singletonList("default");
        }

        return Collections.singletonList(type);
      }
    };
  }

  private String getDataApiPath(HttpServletRequest request) {
    return ((String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE))
        .replace(DataController.PATH_PREFIX, "");
  }
}
