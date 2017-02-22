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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import software.betamax.Headers;
import software.betamax.handler.NonWritableTapeException;
import software.betamax.tape.Tape;

import static com.google.common.net.HttpHeaders.VIA;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class BetamaxInterceptor implements Interceptor {
  private Optional<Tape> tape = Optional.absent();
  private boolean isRunning;
  private static final Logger LOG = LoggerFactory.getLogger(BetamaxInterceptor.class.getName());

  @Override public Response intercept(Chain chain) throws IOException {
    Request request = chain.request();
    if (isRunning) {
      if (!tape.isPresent()) {
        return new Response.Builder() //
            .protocol(Protocol.HTTP_1_1)  //
            .code(403) //
            .body(ResponseBody.create(MediaType.parse("text/plain"), "No tape")) //
            .build();
      } else {
        //noinspection ConstantConditions
        Tape tape = this.tape.get();
        if (tape.isReadable() && tape.seek(request)) {
          LOG.warn(String.format("Playing back from tape %s", tape.getName()));
          Response response = tape.play(request);
          response = setBetamaxHeader(response, "PLAY");
          response = setViaHeader(response);
          return response;
        } else {
          LOG.warn(String.format("no matching request found on %s", tape.getName()));
          Response response = chain.proceed(request);
          response = setBetamaxHeader(response, "REC");
          response = setViaHeader(response);
          if (tape.isWritable()) {
            LOG.info(String.format("Recording to tape %s", tape.getName()));
            tape.record(request, response);
          } else {
            throw new NonWritableTapeException();
          }
          return response;
        }
      }
    } else {
      return chain.proceed(request);
    }
  }

  private Response setViaHeader(Response response) {
    return response.newBuilder() //
        .addHeader(VIA, Headers.VIA_HEADER) //
        .build();
  }

  private Response setBetamaxHeader(Response response, String value) {
    return response.newBuilder() //
        .addHeader(Headers.X_BETAMAX, value) //
        .build();
  }

  public void start(Tape tape) {
    isRunning = true;
    this.tape = Optional.of(tape);
  }

  public void stop() {
    isRunning = false;
  }
}
