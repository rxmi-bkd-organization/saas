package org.bkd.saas.shared.dto;

public record ErrorDto(Long timestamp, Integer status, String error, String message, String path) {
}
