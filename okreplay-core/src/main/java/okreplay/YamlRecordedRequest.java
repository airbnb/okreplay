package okreplay;

import java.net.URI;
import java.util.Collections;
import java.util.Map;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.internal.http.HttpMethod;

public class YamlRecordedRequest extends YamlRecordedMessage {
  private final String method;
  private final URI uri;

  YamlRecordedRequest(Map<String, String> headers, Object body, String method, URI uri) {
    super(headers, body);
    this.method = method;
    this.uri = uri;
  }

  /** For SnakeYAML */
  @SuppressWarnings("unused") public YamlRecordedRequest() {
    this(Collections.<String, String>emptyMap(), null, null, null);
  }

  public String method() {
    return method;
  }

  public URI uri() {
    return uri;
  }

  @Override Request toImmutable() {
    Object body = body();
    MediaType mediaType = MediaType.parse(contentType());
    RequestBody requestBody = null;
    if (body != null) {
      requestBody = body instanceof String
          ? RequestBody.create(mediaType, (String) body)
          : RequestBody.create(mediaType, (byte[]) body);
    } else if (HttpMethod.requiresRequestBody(method)) {
      // The method required a body but none was given. Use an empty one.
      requestBody = RequestBody.create(mediaType, new byte[0]);
    }
    return new RecordedRequest.Builder()
        .headers(okhttp3.Headers.of(headers()))
        .method(method, requestBody)
        .url(HttpUrl.get(uri))
        .build();
  }
}
