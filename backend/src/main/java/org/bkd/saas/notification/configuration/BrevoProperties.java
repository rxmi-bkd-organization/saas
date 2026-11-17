package org.bkd.saas.notification.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.brevo")
public record BrevoProperties(boolean enabled, String apiKey, String senderEmail, String senderName, String baseUrl) {
}
