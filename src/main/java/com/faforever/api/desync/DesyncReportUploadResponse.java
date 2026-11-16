package com.faforever.api.desync;

import java.util.Map;

/**
 * @param url the presigned url to PUT the report to
 * @param key the object key the report will be stored under
 * @param headers headers that have to be sent with the PUT request exactly as given (they are part of the signature)
 */
public record DesyncReportUploadResponse(String url, String key, Map<String, String> headers) {
}
