package com.faforever.api.email;

import com.google.common.collect.Sets;

import java.util.Set;

public interface EmailSender {
  default void sendMail(String fromEmail, String fromName, String toEmail, String subject, String content) {
    sendMail(fromEmail, fromName, Sets.newHashSet(toEmail), subject, content);
  }

  void sendMail(String fromEmail, String fromName, Set<String> toEmails, String subject, String content);
}
