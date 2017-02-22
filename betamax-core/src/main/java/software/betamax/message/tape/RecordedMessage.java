package software.betamax.message.tape;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import okhttp3.Headers;

public abstract class RecordedMessage extends AbstractMessage implements Message {
  final Headers headers;
  final Object body;

  RecordedMessage(Headers headers, Object body) {
    this.headers = headers;
    this.body = body;
  }

  public final boolean hasBody() {
    return body != null;
  }

  protected final InputStream getBodyAsStream() throws UnsupportedEncodingException {
    byte[] bytes;
    if (hasBody()) {
      bytes = body instanceof String ? ((String) body).getBytes(getCharset()) : (byte[]) body;
    } else {
      bytes = new byte[0];
    }

    return new ByteArrayInputStream(bytes);
  }

  public Headers headers() {
    return headers;
  }

  public Object getBody() {
    return body;
  }
}