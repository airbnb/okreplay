package okreplay;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import okhttp3.Headers;
import okhttp3.MediaType;

abstract class RecordedMessage extends AbstractMessage {
  final Headers headers;
  final byte[] body;

  RecordedMessage(Headers headers, byte[] body) {
    this.headers = headers;
    this.body = body;
  }

  @Override public final boolean hasBody() {
    return body != null;
  }

  @Override public Headers headers() {
    return headers;
  }

  @Override public byte[] body() {
    return body;
  }

  LinkedHashMap<String, String> headersAsMap() {
    Map<String, String> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    for (int i = 0, size = headers.size(); i < size; i++) {
      String name = headers.name(i);
      result.put(name, headers.value(i));
    }
    return new LinkedHashMap<>(result);
  }
}