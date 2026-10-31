package org.bkd.saas.shared.dto;

import java.time.Instant;

public record ErrorDto(Instant timestamp, Integer status, String error, String message, String path) {
}
