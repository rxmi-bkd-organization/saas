package org.bkd.saas.notification.service;

import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.bkd.saas.notification.configuration.BrevoProperties;
import org.bkd.saas.notification.exception.EmailSendException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
@RequiredArgsConstructor
public class BrevoService {

  private static final String SEND_EMAIL_PATH = "/v3/smtp/email";
  private static final String API_KEY_HEADER = "api-key";
  private static final String SENDER_FIELD = "sender";
  private static final String RECIPIENTS_FIELD = "to";
  private static final String SUBJECT_FIELD = "subject";
  private static final String TEXT_CONTENT_FIELD = "textContent";
  private static final String NAME_FIELD = "name";
  private static final String EMAIL_FIELD = "email";

  private final RestClient restClient;
  private final BrevoProperties brevoProperties;

  public void sendEmail(String recipient, String subject, String body) {
    if (!brevoProperties.enabled()) {
      return;
    }

    Map<String, String> senderDetails = Map.of(NAME_FIELD,
                                               brevoProperties.senderName(),
                                               EMAIL_FIELD,
                                               brevoProperties.senderEmail());

    List<Map<String, String>> recipientDetails = List.of(Map.of(EMAIL_FIELD, recipient));

    Map<String, Object> payload = Map.of(SENDER_FIELD,
                                         senderDetails,
                                         RECIPIENTS_FIELD,
                                         recipientDetails,
                                         SUBJECT_FIELD,
                                         subject,
                                         TEXT_CONTENT_FIELD,
                                         body);

    post(payload);
  }

  private void post(Map<String, Object> payload) {
    try {
      restClient.post()
                .uri(brevoProperties.baseUrl() + SEND_EMAIL_PATH)
                .header(API_KEY_HEADER, brevoProperties.apiKey())
                .body(payload)
                .retrieve()
                .toBodilessEntity();
    }
    catch (RestClientException e) {
      throw new EmailSendException(e);
    }
  }
}
