package okreplay;

import java.nio.charset.Charset;

import okhttp3.MediaType;

import static okreplay.Util.CONTENT_TYPE;
import static okreplay.Util.isNullOrEmpty;

abstract class AbstractMessage implements Message {
  private static final String CONTENT_ENCODING = "Content-Encoding";
  private static final Charset UTF_8 = Charset.forName("UTF-8");
  static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";
  private static final String DEFAULT_ENCODING = "none";

  @Override public String getContentType() {
    String header = header(CONTENT_TYPE);
    if (isNullOrEmpty(header)) {
      return DEFAULT_CONTENT_TYPE;
    } else {
      return MediaType.parse(header).toString();
    }
  }

  @Override public Charset getCharset() {
    String header = header(CONTENT_TYPE);
    if (isNullOrEmpty(header)) {
      // TODO: this isn't valid for non-text data – this method should return Optional<String>
      return UTF_8;
    } else {
      return Optional.fromNullable(MediaType.parse(header).charset()).or(UTF_8);
    }
  }

  @Override public String getEncoding() {
    String header = header(CONTENT_ENCODING);
    return header == null || header.length() == 0 ? DEFAULT_ENCODING : header;
  }

  @Override public String header(String name) {
    return headers().get(name);
  }

  @Override public final String bodyAsText() {
    return new String(body(), getCharset());
  }
}
