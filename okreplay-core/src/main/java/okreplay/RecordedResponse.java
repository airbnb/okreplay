package okreplay;

import java.io.IOException;
import java.util.Map;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.ResponseBody;

class RecordedResponse extends RecordedMessage implements Response {
  private final int code;
  private final Protocol protocol;

  private RecordedResponse(Builder builder) {
    super(builder.headers.build(), builder.body);
    this.code = builder.code;
    this.protocol = builder.protocol;
  }

  RecordedResponse(int code, Map<String, String> headers, byte[] body) {
    super(Headers.of(headers), body);
    this.code = code;
    this.protocol = Protocol.HTTP_1_1;
  }

  @Override public int code() {
    return code;
  }

  @Override public Builder newBuilder() {
    return new Builder(this);
  }

  @Override public Protocol protocol() {
    return protocol;
  }

  @Override public YamlRecordedResponse toYaml() {
    return new YamlRecordedResponse(headersAsMap(), maybeBodyAsString(), code);
  }

  static class Builder {
    private Protocol protocol = Protocol.HTTP_1_1;
    private int code = -1;
    private Headers.Builder headers;
    private byte[] body;

    Builder() {
      headers = new Headers.Builder();
    }

    private Builder(RecordedResponse response) {
      this.code = response.code;
      this.headers = response.headers.newBuilder();
      this.body = response.body;
      this.protocol = response.protocol;
    }

    Builder protocol(Protocol protocol) {
      this.protocol = protocol;
      return this;
    }

    Builder code(int code) {
      this.code = code;
      return this;
    }

    /**
     * Sets the header named {@code name} to {@code value}. If this request already has any headers
     * with that name, they are all replaced.
     */
    Builder header(String name, String value) {
      headers.set(name, value);
      return this;
    }

    /**
     * Adds a header with {@code name} and {@code value}. Prefer this method for multiply-valued
     * headers like "Set-Cookie".
     */
    Builder addHeader(String name, String value) {
      headers.add(name, value);
      return this;
    }

    Builder removeHeader(String name) {
      headers.removeAll(name);
      return this;
    }

    /** Removes all headers on this builder and adds {@code headers}. */
    Builder headers(Headers headers) {
      this.headers = headers.newBuilder();
      return this;
    }

    Builder body(ResponseBody body) {
      try {
        this.body = body.bytes();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      MediaType contentType = body.contentType();
      if (contentType != null && headers.get(Util.CONTENT_TYPE) == null) {
        addHeader(Util.CONTENT_TYPE, contentType.toString());
      }
      return this;
    }

    RecordedResponse build() {
      if (code < 0)
        throw new IllegalStateException("code < 0: " + code);
      return new RecordedResponse(this);
    }
  }
}