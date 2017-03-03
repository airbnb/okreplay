package walkman;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.net.HttpHeaders.VIA;
import static java.util.Collections.unmodifiableList;

/**
 * Represents a set of recorded HTTP interactions that can be played back or
 * appended to.
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
abstract class MemoryTape implements Tape {
  private String name;
  private List<RecordedInteraction> interactions = Lists.newArrayList();
  private transient TapeMode mode = WalkmanConfig.DEFAULT_MODE;
  private transient MatchRule matchRule = WalkmanConfig.DEFAULT_MATCH_RULE;
  private final transient AtomicInteger orderedIndex = new AtomicInteger();

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
        Request nextRequest = interaction == null ? null : interaction.request();
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

      if (!matchRule.isMatch(request, nextInteraction.request())) {
        throw new IllegalStateException(String.format("Request %s does not match recorded " +
            "request" + " %s", stringify(request), stringify(nextInteraction.request())));
      }

      return nextInteraction.response();
    } else {
      int position = findMatch(request);
      if (position < 0) {
        throw new IllegalStateException("no matching recording found");
      } else {
        return interactions.get(position).response();
      }
    }
  }

  private String stringify(Request request) {
    byte[] body = request.getBody() != null ? request.getBody() : new byte[0];
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
        return matchRule.isMatch(request, input.request());
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
        .removeHeader(Headers.X_WALKMAN) //
        .build();
  }
}
