package okreplay;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.BufferedSource;

class OkHttpResponseAdapter {
  /** Construct a OkHttp Response from a previously recorded interaction */
  static okhttp3.Response adapt(okhttp3.Request okhttpRequest, Response recordedResponse) {
    ResponseBody responseBody = ResponseBody.create(
        MediaType.parse(recordedResponse.getContentType()), recordedResponse.body());
    return new okhttp3.Response.Builder()
        .headers(recordedResponse.headers())
        .body(responseBody)
        .code(recordedResponse.code())
        .protocol(recordedResponse.protocol())
        .request(okhttpRequest)
        .message("")
        .build();
  }

  /** Construct a OkReplay Response based on the provided OkHttp response */
  static Response adapt(final okhttp3.Response okhttpResponse, ResponseBody body) {
    return new RecordedResponse.Builder()
        .headers(okhttpResponse.headers())
        .body(body)
        .protocol(okhttpResponse.protocol())
        .code(okhttpResponse.code())
        .build();
  }

  static ResponseBody cloneResponseBody(ResponseBody responseBody) {
    try {
      BufferedSource source = responseBody.source();
      source.request(Long.MAX_VALUE);
      return ResponseBody.create(responseBody.contentType(), responseBody.contentLength(),
          source.buffer().clone());
    } catch (IOException e) {
      throw new RuntimeException("Failed to read response body", e);
    }
  }
}
