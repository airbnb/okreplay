package okreplay;

import java.net.URI;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.internal.http.HttpMethod;

public class RecordedRequestJavabean extends RecordedMessageJavabean {
  private String method;
  private URI uri;

  public String method() {
    return method;
  }

  public void setMethod(String method) {
    this.method = method;
  }

  public URI uri() {
    return uri;
  }

  public void setUri(URI uri) {
    this.uri = uri;
  }

  Request toImmutable() {
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
