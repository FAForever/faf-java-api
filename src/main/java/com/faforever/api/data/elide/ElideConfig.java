package com.faforever.api.data.elide;

import com.faforever.api.data.elide.checks.IsOwner;
import com.yahoo.elide.Elide;
import com.yahoo.elide.audit.Slf4jLogger;
import com.yahoo.elide.core.EntityDictionary;
import com.yahoo.elide.core.filter.dialect.RSQLFilterDialect;
import com.yahoo.elide.datastores.hibernate5.HibernateStore;
import com.yahoo.elide.security.checks.Check;
import org.hibernate.SessionFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.AbstractCacheResolver;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;
import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class ElideConfig {

  @Bean
  public Elide elide(EntityManagerFactory entityManagerFactory) {
    ConcurrentHashMap<String, Class<? extends Check>> checks = new ConcurrentHashMap<>();
    checks.put("user is this user", IsOwner.Inline.class);

    EntityDictionary entityDictionary = new EntityDictionary(checks);

    RSQLFilterDialect rsqlFilterDialect = new RSQLFilterDialect(entityDictionary);

    return new Elide.Builder(new HibernateStore(entityManagerFactory.unwrap(SessionFactory.class)))
        .withAuditLogger(new Slf4jLogger())
        .withEntityDictionary(entityDictionary)
        .withJoinFilterDialect(rsqlFilterDialect)
        .withSubqueryFilterDialect(rsqlFilterDialect)
        .build();
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
        String jsonApiPath = JsonApiController.getJsonApiPath((HttpServletRequest) context.getArgs()[1]);
        String type = jsonApiPath.split("/")[0];

        if (!cacheManager.getCacheNames().contains(type)) {
          return Collections.singletonList("default");
        }

        return Collections.singletonList(type);
      }
    };
  }
}
