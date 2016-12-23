package com.faforever.api.data.elide;

import com.yahoo.elide.Elide;
import com.yahoo.elide.audit.Slf4jLogger;
import com.yahoo.elide.datastores.hibernate5.HibernateStore;
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

@Configuration
public class ElideConfig {

  @Bean
  public Elide elide(EntityManagerFactory entityManagerFactory) {
    return new Elide.Builder(new HibernateStore(entityManagerFactory.unwrap(SessionFactory.class)))
        .withAuditLogger(new Slf4jLogger())
        .build();
  }

  /**
   * Returns a cache resolver that resolves cache names by JSON API type names. For instance, the type "maps" will be
   * resolved to a cache named "maps".
   */
  @Bean
  public CacheResolver elideCacheResolver(CacheManager cacheManager) {
    return new AbstractCacheResolver(cacheManager) {
      @Override
      protected Collection<String> getCacheNames(CacheOperationInvocationContext<?> context) {
        String jsonApiPath = ElideController.getJsonApiPath((HttpServletRequest) context.getArgs()[1]);
        String[] pathSegments = jsonApiPath.split("/");
        return Collections.singletonList(pathSegments[1]);
      }
    };
  }
}
