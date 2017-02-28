package software.betamax.proxy.okhttp;

import software.betamax.message.tape.RecordedRequest;
import software.betamax.message.tape.Request;

public class OkHttpRequestAdapter {
  /** Construct a Betamax Request based on the provided OkHttp Request */
  public static Request adapt(okhttp3.Request request) {
    return new RecordedRequest.Builder()
        .url(request.url())
        .method(request.method(), request.body())
        .headers(request.headers())
        .build();
  }
}
