package okreplay;

import java.util.Collections;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.ResponseBody;

public class YamlRecordedResponse extends YamlRecordedMessage {
  private final int status;

  YamlRecordedResponse(Map<String, String> headers, Object body, int status) {
    super(headers, body);
    this.status = status;
  }

  /** For SnakeYAML */
  @SuppressWarnings("unused") public YamlRecordedResponse() {
    this(Collections.<String, String>emptyMap(), null, 0);
  }

  public int code() {
    return status;
  }

  @Override Response toImmutable() {
    Object body = body();
    MediaType mediaType = MediaType.parse(contentType());
    ResponseBody responseBody = null;
    if (body != null) {
      responseBody = body instanceof String
          ? ResponseBody.create(mediaType, (String) body)
          : ResponseBody.create(mediaType, (byte[]) body);
    }
    return new RecordedResponse.Builder()
        .code(code())
        .headers(okhttp3.Headers.of(headers()))
        .body(responseBody)
        .build();
  }
}
