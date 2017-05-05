package okreplay;

class OkHttpRequestAdapter {
  /** Construct a OkReplay Request based on the provided OkHttp Request */
  static Request adapt(okhttp3.Request request) {
    return new RecordedRequest.Builder()
        .url(request.url())
        .method(request.method(), request.body())
        .headers(request.headers())
        .build();
  }
}
