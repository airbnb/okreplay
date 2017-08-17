package okreplay;

import java.nio.charset.Charset;

import okhttp3.Headers;

/**
 * An abstraction of an HTTP request or response. Implementations can be backed by any sort of
 * underlying implementation.
 */
interface Message {
  /** @return all HTTP headers attached to this message. */
  Headers headers();

  /**
   * @param name an HTTP header name.
   * @return the comma-separated values for all HTTP headers with the specified name or `null` if
   * there are no headers with that name.
   */
  String header(String name);

  /** @return `true` if the message currently contains a body, `false` otherwise. */
  boolean hasBody();

  /**
   * Returns the message body as a string.
   *
   * @return the message body as a string.
   * @throws IllegalStateException if the message does not have a body.
   */
  String bodyAsText();

  /**
   * Returns the decoded message body. If the implementation stores the message body in an encoded
   * form (e.g. gzipped) then it <em>must</em> be decoded before being returned by this method
   *
   * @return the message body as binary data.
   * @throws IllegalStateException if the message does not have a body.
   */
  byte[] body();

  /** @return the MIME content type of the message not including any charset. */
  String getContentType();

  /** @return the charset of the message if it is text. */
  Charset getCharset();

  /** @return the content encoding of the message, e.g. _gzip_, _deflate_ or _none_. */
  String getEncoding();

  /** @return a copy of this Request object ready to be serialized to YAML */
  YamlRecordedMessage toYaml();
}
