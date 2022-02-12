package com.faforever.api.email;

import com.faforever.api.config.FafApiProperties;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.text.MessageFormat.format;

@Component
@RequiredArgsConstructor
@Slf4j
public class MailBodyBuilder implements InitializingBean {

  @Getter
  public enum Template {
    ACCOUNT_ACTIVATION("username", "activationUrl"),
    WELCOME_TO_FAF("username"),
    PASSWORD_RESET("username", "passwordResetUrl");

    private final Set<String> variables;

    Template(String... variables) {
      this.variables = Set.of(variables);
    }
  }

  private final FafApiProperties properties;

  private Path getTemplateFilePath(Template template) {
    final var path = switch (template) {
      case ACCOUNT_ACTIVATION -> properties.getRegistration().getActivationMailTemplatePath();
      case WELCOME_TO_FAF -> properties.getRegistration().getWelcomeMailTemplatePath();
      case PASSWORD_RESET -> properties.getPasswordReset().getMailTemplatePath();
    };

    return Path.of(path);
  }

  @Override
  public void afterPropertiesSet(){
    boolean templateError = false;

    for (final var template : Template.values()) {
      var path = getTemplateFilePath(template);

      if (Files.exists(path)) {
        log.debug("Template {} has template file present at {}", template, path);
      } else {
        templateError = true;
        log.error("Template {} is missing file at configurate destination: {}", template, path);
      }

      try {
        loadAndValidateTemplate(template);
      } catch (Exception e) {
        log.error("Template {} has invalid template file at {}. Error: {}", template, path, e.getMessage());
        templateError = true;
      }
    }

    if (templateError) {
      throw new IllegalStateException("At least one template file is not available or inconsistent.");
    }

    log.info("All template files present.");
  }

  private String loadAndValidateTemplate(Template template) throws IOException {
    final var templateBody = Files.readString(getTemplateFilePath(template));

    final var missingVariables = template.variables.stream()
      .map(variable -> "{{" + variable + "}}")
      .filter(variable -> !templateBody.contains(variable))
      .collect(Collectors.joining(", "));

    if (missingVariables.length() > 0) {
      throw new IllegalStateException(format("Template file for {0} is missing variables: {1}", template, missingVariables));
    }

    return templateBody;
  }

  private void validateVariables(Template template, Set<String> variables) {
    final var missingVariables = template.variables.stream()
      .filter(variable -> !variables.contains(variable))
      .collect(Collectors.joining(", "));

    final var unknownVariables = variables.stream()
      .filter(variable -> !template.variables.contains(variable))
      .collect(Collectors.joining(", "));

    if (unknownVariables.length() > 0) {
      log.warn("Unknown variable(s) handed over for template {}: {}", template, unknownVariables);
    }

    if (missingVariables.length() > 0) {
      throw new IllegalArgumentException("Variable(s) not assigned: " + missingVariables);
    }
  }

  private String populate(Template template, Map<String, String> variables) throws IOException {
    validateVariables(template, variables.keySet());

    var templateBody = loadAndValidateTemplate(template);

    log.trace("Raw template body: {}", templateBody);

    for (var entry : variables.entrySet()) {
      final var variable = "{{" + entry.getKey() + "}}";
      final var value = entry.getValue();

      log.trace("Replacing {} with {}", variable, value);
      templateBody = templateBody.replace(variable, value);
    }

    log.trace("Replaced template body: {}", templateBody);

    return templateBody;
  }

  public String buildAccountActivationBody(String username, String activationUrl) throws IOException {
    return populate(Template.ACCOUNT_ACTIVATION, Map.of(
      "username", username,
      "activationUrl", activationUrl
    ));
  }

  public String buildPasswordResetBody(String username, String passwordResetUrl) throws IOException {
    return populate(Template.PASSWORD_RESET, Map.of(
      "username", username,
      "passwordResetUrl", passwordResetUrl
    ));
  }
}
