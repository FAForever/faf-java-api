package com.faforever.api.email;

import com.faforever.api.config.FafApiProperties;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class MailBodyBuilderTest {

  private static final String TEMPLATE_FILE = "templateFile";

  @TempDir
  private Path tempDir;

  private MailBodyBuilder instance;

  private final FafApiProperties properties = new FafApiProperties();

  @BeforeEach
  public void beforeEach() {
    properties.getRegistration().setActivationUrlFormat("someActivationUrl");
    properties.getRegistration().setActivationMailTemplatePath(tempDir.resolve(TEMPLATE_FILE).toString());
    properties.getRegistration().setWelcomeMailTemplatePath(tempDir.resolve(TEMPLATE_FILE).toString());
    properties.getPasswordReset().setPasswordResetUrlFormat("somePasswordResetUrl");
    properties.getPasswordReset().setMailTemplatePath(tempDir.resolve(TEMPLATE_FILE).toString());

    instance = new MailBodyBuilder(properties);
  }

  @SneakyThrows
  private void writeTemplateFile(String data) {
    Files.writeString(tempDir.resolve(TEMPLATE_FILE), data);
  }

  @Test
  public void initFailsOnMissingTemplateFile() {
    var result = assertThrows(Exception.class, () -> instance.afterPropertiesSet());

    assertThat(result.getMessage(), is("At least one template file is not available or inconsistent."));
  }

  @Test
  public void initFailsOnMissingVariablesInTemplateFile() {
    writeTemplateFile("{{username}} {{activationUrl}}");

    var result = assertThrows(Exception.class, () -> instance.afterPropertiesSet());

    assertThat(result.getMessage(), is("At least one template file is not available or inconsistent."));
  }

  @Test
  public void initSucceeds() {
    writeTemplateFile("{{username}} {{activationUrl}} {{passwordResetUrl}}");

    instance.afterPropertiesSet();
  }

  @Test
  public void populateFailsOnMissingVariables() throws Exception {
    writeTemplateFile("I forgot the variables {{and put in wrong ones}}");

    var result = assertThrows(IllegalStateException.class, () ->instance.buildAccountActivationBody("junit", "someActionUrl"));

    assertThat(result.getMessage(), startsWith("Template file for ACCOUNT_ACTIVATION is missing variables:"));
  }

  @Test
  public void buildAccountActivationBodySucceeds() throws Exception {
    writeTemplateFile("{{username}} {{activationUrl}}");

    var result = instance.buildAccountActivationBody("junit", "someActionUrl");

    assertThat(result, is("junit someActionUrl"));
  }

  @Test
  public void buildPasswordResetBodySucceeds() throws Exception {
    writeTemplateFile("{{username}} {{passwordResetUrl}}");

    var result = instance.buildPasswordResetBody("junit", "someActionUrl");

    assertThat(result, is("junit someActionUrl"));
  }
}
