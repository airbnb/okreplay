package okreplay;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;

import okhttp3.CacheControl;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.internal.http.HttpMethod;
import okio.Buffer;

import static com.google.common.net.HttpHeaders.CONTENT_TYPE;

public class RecordedRequest extends RecordedMessage implements Request {
  private final String method;
  private final HttpUrl url;

  private RecordedRequest(Builder builder) {
    super(builder.headers.build(), builder.body);
    this.url = builder.url;
    this.method = builder.method;
  }

  public RecordedRequest(String method, String url) {
    this(method, url, Collections.<String, String>emptyMap());
  }

  public RecordedRequest(String method, String url, Map<String, String> headers) {
    this(method, url, headers, null);
  }

  public RecordedRequest(String method, String url, Map<String, String> headers, byte[] body) {
    super(Headers.of(headers), body);
    this.method = method;
    this.url = HttpUrl.parse(url);
  }

  public String method() {
    return method;
  }

  public HttpUrl url() {
    return url;
  }

  @Override public Builder newBuilder() {
    return new Builder(this);
  }

  public static class Builder {
    private HttpUrl url;
    private String method;
    private Headers.Builder headers;
    private byte[] body;

    public Builder() {
      this.method = "GET";
      this.headers = new Headers.Builder();
    }

    private Builder(RecordedRequest request) {
      this.url = request.url;
      this.method = request.method;
      this.body = request.body;
      this.headers = request.headers.newBuilder();
    }

    public Builder url(HttpUrl url) {
      if (url == null)
        throw new NullPointerException("url == null");
      this.url = url;
      return this;
    }

    /**
     * Sets the URL target of this request.
     *
     * @throws IllegalArgumentException if {@code url} is not a valid HTTP or HTTPS URL. Avoid this
     *                                  exception by calling {@link HttpUrl#parse}; it returns null
     *                                  for invalid URLs.
     */
    public Builder url(String url) {
      if (url == null)
        throw new NullPointerException("url == null");

      // Silently replace websocket URLs with HTTP URLs.
      if (url.regionMatches(true, 0, "ws:", 0, 3)) {
        url = "http:" + url.substring(3);
      } else if (url.regionMatches(true, 0, "wss:", 0, 4)) {
        url = "https:" + url.substring(4);
      }

      HttpUrl parsed = HttpUrl.parse(url);
      if (parsed == null)
        throw new IllegalArgumentException("unexpected url: " + url);
      return url(parsed);
    }

    /**
     * Sets the URL target of this request.
     *
     * @throws IllegalArgumentException if the scheme of {@code url} is not {@code http} or {@code
     *                                  https}.
     */
    public Builder url(URL url) {
      if (url == null)
        throw new NullPointerException("url == null");
      HttpUrl parsed = HttpUrl.get(url);
      if (parsed == null)
        throw new IllegalArgumentException("unexpected url: " + url);
      return url(parsed);
    }

    /**
     * Sets the header named {@code name} to {@code value}. If this request already has any headers
     * with that name, they are all replaced.
     */
    public Builder header(String name, String value) {
      headers.set(name, value);
      return this;
    }

    /**
     * Adds a header with {@code name} and {@code value}. Prefer this method for multiply-valued
     * headers like "Cookie".
     *
     * <p>Note that for some headers including {@code Content-Length} and {@code Content-Encoding},
     * OkHttp may replace {@code value} with a header derived from the request body.
     */
    public Builder addHeader(String name, String value) {
      headers.add(name, value);
      return this;
    }

    public Builder removeHeader(String name) {
      headers.removeAll(name);
      return this;
    }

    /** Removes all headers on this builder and adds {@code headers}. */
    public Builder headers(Headers headers) {
      this.headers = headers.newBuilder();
      return this;
    }

    /**
     * Sets this request's {@code Cache-Control} header, replacing any cache control headers already
     * present. If {@code cacheControl} doesn't define any directives, this clears this request's
     * cache-control headers.
     */
    public Builder cacheControl(CacheControl cacheControl) {
      String value = cacheControl.toString();
      if (value.isEmpty())
        return removeHeader("Cache-Control");
      return header("Cache-Control", value);
    }

    public Builder get() {
      return method("GET", null);
    }

    public Builder head() {
      return method("HEAD", null);
    }

    public Builder post(RequestBody body) {
      return method("POST", body);
    }

    public Builder delete(RequestBody body) {
      return method("DELETE", body);
    }

    public Builder delete() {
      return delete(RequestBody.create(null, new byte[0]));
    }

    public Builder put(RequestBody body) {
      return method("PUT", body);
    }

    public Builder patch(RequestBody body) {
      return method("PATCH", body);
    }

    public Builder method(String method, RequestBody body) {
      if (method == null)
        throw new NullPointerException("method == null");
      if (method.length() == 0)
        throw new IllegalArgumentException("method.length() == 0");
      if (body != null && !HttpMethod.permitsRequestBody(method)) {
        throw new IllegalArgumentException("method " + method + " must not have a request body.");
      }
      if (body == null && HttpMethod.requiresRequestBody(method)) {
        throw new IllegalArgumentException("method " + method + " must have a request body.");
      }
      this.method = method;
      if (body != null) {
        try {
          Buffer buffer = new Buffer();
          body.writeTo(buffer);
          this.body = buffer.readByteArray();
          MediaType contentType = body.contentType();
          if (contentType != null) {
            addHeader(CONTENT_TYPE, contentType.toString());
          }
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
      return this;
    }

    public RecordedRequest build() {
      if (url == null)
        throw new IllegalStateException("url == null");
      return new RecordedRequest(this);
    }
  }
}