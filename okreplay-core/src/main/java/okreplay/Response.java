package okreplay;

import okhttp3.Protocol;

public interface Response extends Message {
  /** @return the HTTP status code of the response. */
  int code();
  /** @return the content MIME type of the response. */
  String getContentType();
  RecordedResponse.Builder newBuilder();
  Protocol protocol();
}