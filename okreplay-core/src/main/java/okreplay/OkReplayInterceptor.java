package okreplay;

import java.io.IOException;
import java.util.logging.Logger;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.ResponseBody;

import static okreplay.Util.VIA;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class OkReplayInterceptor implements Interceptor {
  private OkReplayConfig configuration;
  private Optional<Tape> tape = Optional.absent();
  private boolean isRunning;
  private static final Logger LOG = Logger.getLogger(OkReplayInterceptor.class.getName());

  @Override public okhttp3.Response intercept(Chain chain) throws IOException {
    okhttp3.Request request = chain.request();
    if (isRunning && !isHostIgnored(request)) {
      if (!tape.isPresent()) {
        return buildResponse(request, 403, "No tape");
      } else {
        //noinspection ConstantConditions
        Tape tape = this.tape.get();
        Request recordedRequest = OkHttpRequestAdapter.adapt(request);
        if (tape.isReadable() && tape.seek(recordedRequest)) {
          return replayResponse(request, tape, recordedRequest);
        } else {
          LOG.warning(String.format("no matching request found on tape '%s' for request %s %s",
              tape.getName(), request.method(), request.url().toString()));
          if (tape.getMode() == TapeMode.READ_ONLY_QUIET) {
            return buildResponse(request, 404, "No matching response");
          }
          // If the tape isn't writeable, abandon this request. This prevents us from
          // talking to the server for non-mutable tapes.
          if (!tape.isWritable()) {
            throwTapeNotWritable(request.method() + " " + request.url().toString());
          }
          // Continue the request and attempt to write the response to the tape.
          return recordResponse(request, tape, recordedRequest, chain.proceed(request));
        }
      }
    } else {
      return chain.proceed(request);
    }
  }

  private okhttp3.Response replayResponse(okhttp3.Request request, Tape tape, Request
      recordedRequest) {
    LOG.info(String.format("Playing back request %s %s from tape '%s'",
        recordedRequest.method(), recordedRequest.url().toString(), tape.getName()));
    Response recordedResponse = tape.play(recordedRequest);
    okhttp3.Response okhttpResponse = OkHttpResponseAdapter.adapt(request, recordedResponse);
    okhttpResponse = setOkReplayHeader(okhttpResponse, "PLAY");
    okhttpResponse = setViaHeader(okhttpResponse);
    return okhttpResponse;
  }

  private okhttp3.Response recordResponse(okhttp3.Request request, Tape tape,
      Request recordedRequest, okhttp3.Response okhttpResponse) {
    okhttpResponse = setOkReplayHeader(okhttpResponse, "REC");
    okhttpResponse = setViaHeader(okhttpResponse);
    LOG.info(String.format("Recording request %s %s to tape '%s'",
        request.method(), request.url().toString(), tape.getName()));
    ResponseBody bodyClone = OkHttpResponseAdapter.cloneResponseBody(okhttpResponse.body());
    Response recordedResponse = OkHttpResponseAdapter.adapt(okhttpResponse, bodyClone);
    tape.record(recordedRequest, recordedResponse);
    okhttpResponse = okhttpResponse.newBuilder()
        .body(OkHttpResponseAdapter.cloneResponseBody(okhttpResponse.body()))
        .build();
    okhttpResponse.body().close();
    return okhttpResponse;
  }

  private okhttp3.Response buildResponse(okhttp3.Request request, int code, String message) {
    return new okhttp3.Response.Builder() //
        .protocol(Protocol.HTTP_1_1)  //
        .code(code) //
        .message("") //
        .body(ResponseBody.create(MediaType.parse("text/plain"), message)) //
        .request(request) //
        .build();
  }

  private void throwTapeNotWritable(String request) {
    throw new NonWritableTapeException("\n"
        + "================================================================================\n"
        + "An HTTP request has been made that OkReplay does not know how to handle:\n"
        + "  " + request + "\n\n"
        + "Under the current configuration, OkReplay can not find a suitable HTTP interaction\n"
        + "to replay and is prevented from recording new requests. There are a few ways you\n"
        + "can configure OkReplay to handle this request:\n\n"
        + "* If you want OkReplay to record this request and play it back during future test\n"
        + "  runs, you should set your annotation to `@OkReplay(mode = TapeMode.READ_WRITE)`\n"
        + "* If you believe this request has been already recorded, you can update your\n"
        + "  `MatchRule` to make sure it matches one of the recorded requests by updating\n"
        + "  your annotation like `@OkReplay(match = { ... })`. You can also manually fix your\n"
        + "  tape file(s) to make sure a match can be found. Sometimes the same request is\n"
        + "  made with different parameters between multiple test runs causing the match.\n"
        + "  rule to not find a suitable interaction to replay.\n"
        + "================================================================================\n");
  }

  private boolean isHostIgnored(okhttp3.Request request) {
    return configuration.getIgnoreHosts().contains(request.url().host());
  }

  private okhttp3.Response setViaHeader(okhttp3.Response response) {
    return response.newBuilder() //
        .addHeader(VIA, Headers.VIA_HEADER) //
        .build();
  }

  private okhttp3.Response setOkReplayHeader(okhttp3.Response response, String value) {
    return response.newBuilder() //
        .addHeader(Headers.X_OKREPLAY, value) //
        .build();
  }

  public void start(OkReplayConfig configuration, Tape tape) {
    this.configuration = configuration;
    this.tape = Optional.fromNullable(tape);
    isRunning = true;
  }

  public void stop() {
    isRunning = false;
  }
}
