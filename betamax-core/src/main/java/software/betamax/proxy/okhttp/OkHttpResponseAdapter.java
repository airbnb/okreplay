package software.betamax.proxy.okhttp;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import software.betamax.message.tape.RecordedResponse;
import software.betamax.message.tape.Response;

public class OkHttpResponseAdapter {
  /** Construct a OkHttp Response from a previously recorded interaction */
  public static okhttp3.Response adapt(okhttp3.Request okhttpRequest, Response recordedResponse) {
    ResponseBody responseBody = ResponseBody.create(
        MediaType.parse(recordedResponse.getContentType()), recordedResponse.getBody());
    return new okhttp3.Response.Builder()
        .headers(recordedResponse.headers())
        .body(responseBody)
        .code(recordedResponse.code())
        .protocol(recordedResponse.protocol())
        .request(okhttpRequest)
        .build();
  }

  /** Construct a Betamax Response based on the provided OkHttp response */
  public static Response adapt(okhttp3.Response okhttpResponse) {
    return new RecordedResponse.Builder()
        .headers(okhttpResponse.headers())
        .body(okhttpResponse.body())
        .protocol(okhttpResponse.protocol())
        .code(okhttpResponse.code())
        .build();
  }
}
