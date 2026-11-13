package com.faforever.api.config;

import com.faforever.api.data.annotation.Ephemeral;
import com.yahoo.elide.core.datastore.DataStore;
import com.yahoo.elide.datastores.noop.NoopDataStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.util.List;

@Configuration
public class EphemeralDatastoreConfig {

    @Bean
    DataStore ephemeralDataStore() {
        ClassPathScanningCandidateComponentProvider ephemeralScanner = new ClassPathScanningCandidateComponentProvider(
                false);
        ephemeralScanner.addIncludeFilter(new AnnotationTypeFilter(Ephemeral.class));
        List<Class> ephemeralModels = ephemeralScanner.findCandidateComponents("com.faforever.api.data.domain")
                                                      .stream()
                                                      .map(beanDefinition -> {
                                                          try {
                                                              return Class.forName(beanDefinition.getBeanClassName());
                                                          } catch (ClassNotFoundException e) {
                                                              throw new RuntimeException(e);
                                                          }
                                                      })
                                                      .map(Class.class::cast)
                                                      .toList();

        return new NoopDataStore(ephemeralModels);
    }
}
