package com.faforever.api.i18n;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.support.AbstractResourceBasedMessageSource;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class RepositoryMessageSource extends AbstractResourceBasedMessageSource implements InitializingBean {

  private static final Locale FALLBACK_LOCALE = Locale.US;

  private final MessageRepository messageRepository;

  /**
   * Language -&gt; Region -&gt; Key -&gt; Value.
   */
  private Map<String, Map<String, Map<String, String>>> messagesByLanguage;

  private Map<String, Map<String, Map<String, String>>> loadMessages() {
    Map<String, Map<String, Map<String, String>>> messagesByLanguage = new HashMap<>();

    messageRepository.findAll().forEach(message -> messagesByLanguage.computeIfAbsent(message.getLanguage(), s -> new HashMap<>())
        .computeIfAbsent(message.getRegion(), s -> new HashMap<>())
        .put(message.getKey(), message.getValue()));

    return messagesByLanguage;
  }

  @Override
  protected String resolveCodeWithoutArguments(String code, Locale locale) {
    return getText(code, locale);
  }

  @Override
  protected MessageFormat resolveCode(String code, Locale locale) {
    return createMessageFormat(getText(code, locale), locale);
  }

  private String getText(String key, Locale locale) {
    String language = locale.getLanguage();
    String region = locale.getCountry();

    if (!messagesByLanguage.containsKey(language)) {
      if (Objects.equals(language, FALLBACK_LOCALE.getLanguage())) {
        return key;
      }
      return getText(key, FALLBACK_LOCALE);
    }

    Map<String, Map<String, String>> messagesByRegion = this.messagesByLanguage.get(language);
    if (!messagesByRegion.containsKey(region)) {
      if (Objects.equals(region, FALLBACK_LOCALE.getCountry())) {
        return key;
      }
      return getText(key, FALLBACK_LOCALE);
    }

    Map<String, String> messagesByKey = messagesByRegion.get(region);
    if (!messagesByKey.containsKey(key)) {
      if (Objects.equals(language, FALLBACK_LOCALE.getLanguage()) && Objects.equals(region, FALLBACK_LOCALE.getCountry())) {
        return key;
      }
      return getText(key, FALLBACK_LOCALE);
    }

    return messagesByRegion.get(region).get(key);
  }

  @Override
  public void afterPropertiesSet() {
    messagesByLanguage = loadMessages();
  }
}
