package com.yaxinaz.storage;

public record FileUploadResponse(String url, String filename, long sizeBytes, String contentType) {
}
