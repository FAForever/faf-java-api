package com.faforever.api.email;

public interface EmailSender {
  void sendMail(String fromEmail, String fromName, String toEmail, String subject, String content);
}
