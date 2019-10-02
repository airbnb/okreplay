package okreplay

import java.io.IOException
import java.util.logging.Logger

import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.Protocol
import okhttp3.ResponseBody

import okreplay.Util.VIA

class OkReplayInterceptor : Interceptor {
  private var configuration: OkReplayConfig? = null
  private var tape: Tape? = null
  private var isRunning: Boolean = false

  @Throws(IOException::class) override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
    val request = chain.request()
    if (isRunning && !isHostIgnored(request)) {
      if (tape == null) {
        return buildResponse(request, 403, "No tape")
      } else {
        val tape = this.tape!!
        val recordedRequest = OkHttpRequestAdapter.adapt(request)
        if (tape.isReadable && tape.seek(recordedRequest)) {
          return replayResponse(request, tape, recordedRequest)
        } else {
          LOG.warning("no matching request found on tape '${tape.name}' for " +
              "request ${request.method()} ${request.url()}")
          if (tape.mode == TapeMode.READ_ONLY_QUIET) {
            return buildResponse(request, 404, "No matching response")
          }
          // If the tape isn't writeable, abandon this request. This prevents us from
          // talking to the server for non-mutable tapes.
          if (!tape.isWritable) {
            throwTapeNotWritable(request.method() + " " + request.url().toString())
          }
          // Continue the request and attempt to write the response to the tape.
          return recordResponse(request, tape, recordedRequest, chain.proceed(request))
        }
      }
    } else {
      return chain.proceed(request)
    }
  }

  private fun replayResponse(
      request: okhttp3.Request,
      tape: Tape,
      recordedRequest: Request
  ): okhttp3.Response {
    LOG.info(String.format("Playing back request %s %s from tape '%s'",
        recordedRequest.method(), recordedRequest.url().toString(), tape.name))
    val recordedResponse = tape.play(recordedRequest)
    var okhttpResponse = OkHttpResponseAdapter.adapt(request, recordedResponse)
    okhttpResponse = setOkReplayHeader(okhttpResponse, "PLAY")
    okhttpResponse = setViaHeader(okhttpResponse)
    return okhttpResponse
  }

  private fun recordResponse(
      request: okhttp3.Request,
      tape: Tape,
      recordedRequest: Request,
      okhttpResponse: okhttp3.Response
  ): okhttp3.Response {
    var okhttpResponse = okhttpResponse
    okhttpResponse = setOkReplayHeader(okhttpResponse, "REC")
    okhttpResponse = setViaHeader(okhttpResponse)
    LOG.info("Recording request ${request.method()} ${request.url()} to tape '${tape.name}'")
    val bodyClone = OkHttpResponseAdapter.cloneResponseBody(okhttpResponse.body()!!)
    val recordedResponse = OkHttpResponseAdapter.adapt(okhttpResponse, bodyClone)
    tape.record(recordedRequest, recordedResponse)
    okhttpResponse = okhttpResponse.newBuilder()
        .body(OkHttpResponseAdapter.cloneResponseBody(okhttpResponse.body()!!))
        .build()
    okhttpResponse.body()!!.close()
    return okhttpResponse
  }

  private fun buildResponse(
      request: okhttp3.Request,
      code: Int,
      message: String
  ): okhttp3.Response {
    return okhttp3.Response.Builder() //
        .protocol(Protocol.HTTP_1_1)  //
        .code(code) //
        .message("") //
        .body(ResponseBody.create(MediaType.parse("text/plain"), message)) //
        .request(request) //
        .build()
  }

  private fun throwTapeNotWritable(request: String) {
    throw NonWritableTapeException("\n"
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
        + "================================================================================\n")
  }

  private fun isHostIgnored(request: okhttp3.Request): Boolean {
    return configuration!!.ignoreHosts.contains(request.url().host())
  }

  private fun setViaHeader(response: okhttp3.Response): okhttp3.Response {
    return response.newBuilder() //
        .addHeader(VIA, Headers.VIA_HEADER) //
        .build()
  }

  private fun setOkReplayHeader(response: okhttp3.Response, value: String): okhttp3.Response {
    return response.newBuilder() //
        .addHeader(Headers.X_OKREPLAY, value) //
        .build()
  }

  fun start(configuration: OkReplayConfig, tape: Tape?) {
    this.configuration = configuration
    this.tape = tape
    isRunning = true
  }

  fun stop() {
    isRunning = false
  }

  companion object {
    private val LOG = Logger.getLogger(OkReplayInterceptor::class.java.name)
  }
}
