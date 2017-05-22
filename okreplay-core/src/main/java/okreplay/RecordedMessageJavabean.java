package okreplay;

import com.google.common.base.Strings;
import com.google.common.net.MediaType;

import java.util.LinkedHashMap;

import static com.google.common.net.HttpHeaders.CONTENT_TYPE;
import static okreplay.AbstractMessage.DEFAULT_CONTENT_TYPE;

public class RecordedMessageJavabean {
  private LinkedHashMap<String, String> headers = new LinkedHashMap<>();
  private Object body;

  String contentType() {
    String header = headers.get(CONTENT_TYPE);
    if (Strings.isNullOrEmpty(header)) {
      return DEFAULT_CONTENT_TYPE;
    } else {
      return MediaType.parse(header).toString();
    }
  }

  public LinkedHashMap<String, String> headers() {
    return headers;
  }

  public String header(String name) {
    return headers.get(name);
  }

  public void setHeaders(LinkedHashMap<String, String> headers) {
    this.headers = headers;
  }

  public Object body() {
    return body;
  }

  public void setBody(Object body) {
    this.body = body;
  }
}
