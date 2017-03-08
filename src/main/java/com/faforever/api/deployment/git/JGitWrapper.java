package com.faforever.api.deployment.git;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

import static com.github.nocatch.NoCatch.noCatch;
import static org.eclipse.jgit.api.Git.cloneRepository;
import static org.eclipse.jgit.api.Git.open;

@Lazy
@Component
@Slf4j
public class JGitWrapper implements GitWrapper {

  public void clone(String repositoryUri, Path targetDirectory) {
    log.debug("Cloning '{}' to '{}'", repositoryUri, targetDirectory);
    noCatch(() -> cloneRepository()
        .setURI(repositoryUri)
        .setDirectory(targetDirectory.toFile())
        .call());
  }

  @Override
  @SneakyThrows
  public void fetch(Path repoDirectory) {
    log.debug("Fetching remote of '{}'", repoDirectory);
    try (Git git = open(repoDirectory.toFile())) {
      git.fetch().call();
    }
  }

  @Override
  @SneakyThrows
  public void checkoutRef(Path repoDirectory, String ref) {
    log.debug("Checking out '{}' in '{}'", ref, repoDirectory);
    try (Git git = open(repoDirectory.toFile())) {
      git.checkout()
          .setForce(true)
          .setName(ref)
          .call();
    }
  }
}
