package okreplay;

import com.google.common.base.Strings;
import com.google.common.net.MediaType;

import java.util.Map;

import static com.google.common.net.HttpHeaders.CONTENT_TYPE;
import static okreplay.AbstractMessage.DEFAULT_CONTENT_TYPE;

public class RecordedMessageJavabean {
  private final Map<String, String> headers;
  private final Object body;

  RecordedMessageJavabean(Map<String, String> headers, Object body) {
    this.headers = headers;
    this.body = body;
  }

  String contentType() {
    String header = headers.get(CONTENT_TYPE);
    if (Strings.isNullOrEmpty(header)) {
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
}
