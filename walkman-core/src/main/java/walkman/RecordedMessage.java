package walkman;

import okhttp3.Headers;

abstract class RecordedMessage extends AbstractMessage implements Message {
  final Headers headers;
  final byte[] body;

  RecordedMessage(Headers headers, byte[] body) {
    this.headers = headers;
    this.body = body;
  }

  public final boolean hasBody() {
    return body != null;
  }

  public Headers headers() {
    return headers;
  }

  @Override public byte[] getBody() {
    return body;
  }
}