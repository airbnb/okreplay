package software.betamax.message.tape;

import com.google.common.base.Strings;
import com.google.common.net.MediaType;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import okio.Buffer;
import okio.Okio;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.net.HttpHeaders.CONTENT_ENCODING;
import static com.google.common.net.HttpHeaders.CONTENT_TYPE;
import static com.google.common.net.MediaType.OCTET_STREAM;

public abstract class AbstractMessage implements Message {
  private static final String DEFAULT_CONTENT_TYPE = OCTET_STREAM.toString();
  private static final String DEFAULT_CHARSET = UTF_8.toString();
  private static final String DEFAULT_ENCODING = "none";

  @Override public String getContentType() {
    String header = header(CONTENT_TYPE);
    if (Strings.isNullOrEmpty(header)) {
      return DEFAULT_CONTENT_TYPE;
    } else {
      return MediaType.parse(header).withoutParameters().toString();
    }
  }

  @Override public String getCharset() {
    String header = header(CONTENT_TYPE);
    if (Strings.isNullOrEmpty(header)) {
      // TODO: this isn't valid for non-text data – this method should return Optional<String>
      return DEFAULT_CHARSET;
    } else {
      return MediaType.parse(header).charset().or(UTF_8).toString();
    }
  }

  @Override public String getEncoding() {
    String header = header(CONTENT_ENCODING);
    return header == null || header.length() == 0 ? DEFAULT_ENCODING : header;
  }

  @Override public String header(String name) {
    return headers().get(name);
  }

  @Override public final byte[] getBodyAsBinary() {
    try {
      InputStream is = getBodyAsStream();
      Buffer buffer = new Buffer();
      buffer.writeAll(Okio.source(is));
      return buffer.readByteArray();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override public final String getBodyAsText() {
    try {
      return new String(getBodyAsBinary(), getCharset());
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  protected abstract InputStream getBodyAsStream() throws IOException;
}