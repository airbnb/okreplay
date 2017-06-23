package okreplay;

import java.util.Map;

import okhttp3.MediaType;

import static okreplay.AbstractMessage.DEFAULT_CONTENT_TYPE;
import static okreplay.Util.CONTENT_TYPE;
import static okreplay.Util.isNullOrEmpty;

public abstract class YamlRecordedMessage {
  private final Map<String, String> headers;
  private final Object body;

  YamlRecordedMessage(Map<String, String> headers, Object body) {
    this.headers = headers;
    this.body = body;
  }

  String contentType() {
    String header = headers.get(CONTENT_TYPE);
    if (isNullOrEmpty(header)) {
      return DEFAULT_CONTENT_TYPE;
    } else {
      return MediaType.parse(header).toString();
    }
  }

  public Map<String, String> headers() {
    return headers;
  }

  public String header(String name) {
    return headers.get(name);
  }

  public Object body() {
    return body;
  }

  abstract Message toImmutable();
}
