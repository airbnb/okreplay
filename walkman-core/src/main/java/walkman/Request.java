package walkman;

import okhttp3.HttpUrl;

public interface Request extends Message {
  /** @return the request method. */
  String method();
  /** @return the target URL of the request. */
  HttpUrl url();
  RecordedRequest.Builder newBuilder();
}