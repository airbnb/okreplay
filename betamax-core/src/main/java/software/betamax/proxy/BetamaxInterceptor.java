/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package software.betamax.proxy;

import com.google.common.base.Optional;

import java.io.IOException;
import java.util.logging.Logger;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.ResponseBody;
import software.betamax.Configuration;
import software.betamax.Headers;
import software.betamax.handler.NonWritableTapeException;
import software.betamax.message.tape.Request;
import software.betamax.message.tape.Response;
import software.betamax.proxy.okhttp.OkHttpRequestAdapter;
import software.betamax.proxy.okhttp.OkHttpResponseAdapter;
import software.betamax.tape.Tape;

import static com.google.common.net.HttpHeaders.VIA;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class BetamaxInterceptor implements Interceptor {
  private Configuration configuration;
  private Optional<Tape> tape = Optional.absent();
  private boolean isRunning;
  private static final Logger LOG = Logger.getLogger(BetamaxInterceptor.class.getName());

  @Override public okhttp3.Response intercept(Chain chain) throws IOException {
    okhttp3.Request request = chain.request();
    if (isRunning && !isHostIgnored(request)) {
      if (!tape.isPresent()) {
        return new okhttp3.Response.Builder() //
            .protocol(Protocol.HTTP_1_1)  //
            .code(403) //
            .body(ResponseBody.create(MediaType.parse("text/plain"), "No tape")) //
            .request(request) //
            .build();
      } else {
        //noinspection ConstantConditions
        Tape tape = this.tape.get();
        Request recordedRequest = OkHttpRequestAdapter.adapt(request);
        if (tape.isReadable() && tape.seek(recordedRequest)) {
          LOG.warning(String.format("Playing back from tape %s", tape.getName()));
          Response recordedResponse = tape.play(recordedRequest);
          okhttp3.Response okhttpResponse = OkHttpResponseAdapter.adapt(request, recordedResponse);
          okhttpResponse = setBetamaxHeader(okhttpResponse, "PLAY");
          okhttpResponse = setViaHeader(okhttpResponse);
          return okhttpResponse;
        } else {
          LOG.warning(String.format("no matching request found on %s", tape.getName()));
          okhttp3.Response okhttpResponse = chain.proceed(request);
          okhttpResponse = setBetamaxHeader(okhttpResponse, "REC");
          okhttpResponse = setViaHeader(okhttpResponse);
          if (tape.isWritable()) {
            LOG.info(String.format("Recording to tape %s", tape.getName()));
            ResponseBody bodyClone = OkHttpResponseAdapter.cloneResponseBody(okhttpResponse.body());
            Response recordedResponse = OkHttpResponseAdapter.adapt(okhttpResponse, bodyClone);
            tape.record(recordedRequest, recordedResponse);
            okhttpResponse = okhttpResponse.newBuilder()
                .body(OkHttpResponseAdapter.cloneResponseBody(okhttpResponse.body()))
                .build();
          } else {
            throw new NonWritableTapeException();
          }
          return okhttpResponse;
        }
      }
    } else {
      return chain.proceed(request);
    }
  }

  private boolean isHostIgnored(okhttp3.Request request) {
    return configuration.getIgnoreHosts().contains(request.url().host());
  }

  private okhttp3.Response setViaHeader(okhttp3.Response response) {
    return response.newBuilder() //
        .addHeader(VIA, Headers.VIA_HEADER) //
        .build();
  }

  private okhttp3.Response setBetamaxHeader(okhttp3.Response response, String value) {
    return response.newBuilder() //
        .addHeader(Headers.X_BETAMAX, value) //
        .build();
  }

  public void start(Configuration configuration, Tape tape) {
    this.configuration = configuration;
    this.tape = Optional.fromNullable(tape);
    isRunning = true;
  }

  public void stop() {
    isRunning = false;
  }
}
