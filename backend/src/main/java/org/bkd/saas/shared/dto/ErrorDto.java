package org.bkd.saas.shared.dto;

public record ErrorDto(String timestamp, Integer status, String error, String message, String path) {
}
