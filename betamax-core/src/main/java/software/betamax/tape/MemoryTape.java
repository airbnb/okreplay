/*
 * Copyright 2011 the original author or authors.
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

package software.betamax.tape;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.io.EOFException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import okio.Buffer;
import software.betamax.Configuration;
import software.betamax.Headers;
import software.betamax.MatchRule;
import software.betamax.TapeMode;
import software.betamax.handler.NonWritableTapeException;
import software.betamax.message.tape.Request;
import software.betamax.message.tape.Response;

import static com.google.common.net.HttpHeaders.VIA;
import static java.util.Collections.unmodifiableList;

/**
 * Represents a set of recorded HTTP interactions that can be played back or
 * appended to.
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public abstract class MemoryTape implements Tape {
  private String name;
  private List<RecordedInteraction> interactions = Lists.newArrayList();

  private transient TapeMode mode = Configuration.DEFAULT_MODE;
  private transient MatchRule matchRule = Configuration.DEFAULT_MATCH_RULE;

  private transient AtomicInteger orderedIndex = new AtomicInteger();

  @Override public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override public TapeMode getMode() {
    return mode;
  }

  @Override public void setMode(TapeMode mode) {
    this.mode = mode;
  }

  @Override public MatchRule getMatchRule() {
    return this.matchRule;
  }

  @Override public void setMatchRule(MatchRule matchRule) {
    this.matchRule = matchRule;
  }

  @Override public boolean isReadable() {
    return mode.isReadable();
  }

  @Override public boolean isWritable() {
    return mode.isWritable();
  }

  @Override public boolean isSequential() {
    return mode.isSequential();
  }

  @Override public int size() {
    return interactions.size();
  }

  public List<RecordedInteraction> getInteractions() {
    return unmodifiableList(interactions);
  }

  public void setInteractions(List<RecordedInteraction> interactions) {
    this.interactions = Lists.newArrayList(interactions);
  }

  @Override public boolean seek(Request request) {
    if (isSequential()) {
      try {
        // TODO: it's a complete waste of time using an AtomicInteger when this method is called
        // before play in a non-transactional way
        Integer index = orderedIndex.get();
        RecordedInteraction interaction = interactions.get(index);
        Request nextRequest = interaction == null ? null : interaction.getRequest();
        return nextRequest != null && matchRule.isMatch(request, nextRequest);
      } catch (IndexOutOfBoundsException e) {
        throw new NonWritableTapeException();
      }
    } else {
      return findMatch(request) >= 0;
    }
  }

  @Override public Response play(final Request request) {
    if (!mode.isReadable()) {
      throw new IllegalStateException("the tape is not readable");
    }

    if (mode.isSequential()) {
      Integer nextIndex = orderedIndex.getAndIncrement();
      final RecordedInteraction nextInteraction = interactions.get(nextIndex);
      if (nextInteraction == null) {
        throw new IllegalStateException(String.format("No recording found at position %s",
            nextIndex));
      }

      if (!matchRule.isMatch(request, nextInteraction.getRequest())) {
        throw new IllegalStateException(String.format("Request %s does not match recorded " +
            "request" + " %s", stringify(request), stringify(nextInteraction.getRequest())));
      }

      return nextInteraction.getResponse();
    } else {
      int position = findMatch(request);
      if (position < 0) {
        throw new IllegalStateException("no matching recording found");
      } else {
        return interactions.get(position).getResponse();
      }
    }
  }

  private String stringify(Request request) {
    byte[] body = request.getBody();
    String bodyLog = " (binary " + body.length + "-byte body omitted)";
    return "method: " + request.method() + ", " + "uri: " + request.url() + ", " + "headers: " +
        request.headers() + ", " + bodyLog;
  }

  @Override public synchronized void record(Request request, Response response) {
    if (!mode.isWritable()) {
      throw new IllegalStateException("the tape is not writable");
    }

    RecordedInteraction interaction = new RecordedInteraction(new Date(), recordRequest(request),
        recordResponse(response));

    if (mode.isSequential()) {
      interactions.add(interaction);
    } else {
      int position = findMatch(request);
      if (position >= 0) {
        interactions.set(position, interaction);
      } else {
        interactions.add(interaction);
      }
    }
  }

  @Override public String toString() {
    return String.format("Tape[%s]", name);
  }

  private synchronized int findMatch(final Request request) {
    return Iterables.indexOf(interactions, new Predicate<RecordedInteraction>() {
      @Override public boolean apply(RecordedInteraction input) {
        return matchRule.isMatch(request, input.getRequest());
      }
    });
  }

  private Request recordRequest(Request request) {
    return request.newBuilder() //
        .removeHeader(VIA) //
        .build();
  }

  private Response recordResponse(Response response) {
    return response.newBuilder() //
        .removeHeader(VIA) //
        .removeHeader(Headers.X_BETAMAX) //
        .build();
  }

  /**
   * Returns true if the body in question probably contains human readable text. Uses a small sample
   * of code points to detect unicode control characters commonly used in binary file signatures.
   */
  private static boolean isPlaintext(Buffer buffer) {
    try {
      Buffer prefix = new Buffer();
      long byteCount = buffer.size() < 64 ? buffer.size() : 64;
      buffer.copyTo(prefix, 0, byteCount);
      for (int i = 0; i < 16; i++) {
        if (prefix.exhausted()) {
          break;
        }
        int codePoint = prefix.readUtf8CodePoint();
        if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
          return false;
        }
      }
      return true;
    } catch (EOFException e) {
      return false; // Truncated UTF-8 sequence.
    }
  }
}
