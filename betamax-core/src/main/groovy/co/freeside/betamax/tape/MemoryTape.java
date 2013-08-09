package co.freeside.betamax.tape;

import co.freeside.betamax.TapeMode;
import co.freeside.betamax.handler.NonWritableTapeException;
import co.freeside.betamax.message.Request;
import co.freeside.betamax.message.Response;
import co.freeside.betamax.message.tape.RecordedRequest;
import co.freeside.betamax.message.tape.RecordedResponse;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import org.yaml.snakeyaml.reader.StreamReader;

import static co.freeside.betamax.TapeMode.READ_WRITE;
import static co.freeside.betamax.Headers.X_BETAMAX;
import static co.freeside.betamax.MatchRule.*;
import static org.apache.http.HttpHeaders.VIA;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

/**
 * Represents a set of recorded HTTP interactions that can be played back or appended to.
 */
public class MemoryTape implements Tape {
    public void setMode(TapeMode mode) {
        this.mode = mode;
    }

    public void setMatchRules(Comparator<Request>[] matchRules) {
        this.matchRules = matchRules;
    }

    public boolean isReadable() {
        return mode.isReadable();
    }

    public boolean isWritable() {
        return mode.isWritable();
    }

    public boolean isSequential() {
        return mode.isSequential();
    }

    public int size() {
        return interactions.size();
    }

    public boolean seek(Request request) {
        if (isSequential()) {
            try {
                // TODO: it's a complete waste of time using an AtomicInteger when this method is called before play in a non-transactional way
                Integer index = orderedIndex.get();
                RecordedInteraction interaction = interactions.get(index);
                RecordedRequest nextRequest = (interaction == null ? null : interaction.getRequest());
                RequestMatcher requestMatcher = new RequestMatcher(request, matchRules);
                return nextRequest != null && requestMatcher.matches(nextRequest);
            } catch (IndexOutOfBoundsException e) {
                throw new NonWritableTapeException();
            }
        } else {
            return findMatch(request) >= 0;
        }

    }

    public Response play(final Request request) {
        if (!mode.isReadable()) {
            throw new IllegalStateException("the tape is not readable");
        }


        if (mode.isSequential()) {
            RequestMatcher requestMatcher = new RequestMatcher(request, matchRules);
            Integer nextIndex = orderedIndex.getAndIncrement();
            final RecordedInteraction nextInteraction = interactions.get(nextIndex);
            if (nextInteraction == null)
                throw new IllegalStateException("No recording found at position " + String.valueOf(nextIndex));

            if (!requestMatcher.matches(nextInteraction.getRequest()))
                throw new IllegalStateException("Request " + stringify(request) + " does not match recorded request " + stringify(nextInteraction.getRequest()));

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
        try {
            return "method: "  + request.getMethod()  + ", " +
                   "uri: "     + request.getUri()     + ", " +
                   "headers: " + request.getHeaders() + ", " +
                   "body: "    + CharStreams.toString(request.getBodyAsText());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void record(Request request, Response response) {
        if (!mode.isWritable())
            throw new IllegalStateException("the tape is not writable");

        RecordedInteraction interaction = new RecordedInteraction();
        interaction.setRequest(recordRequest(request));
        interaction.setResponse(recordResponse(response));
        interaction.setRecorded(new Date());

        if (mode.isSequential()) {
            interactions.add(interaction);
        } else {
            int position = findMatch(request);
            if (position >= 0)
                interactions.set(position, interaction);
            else
                interactions.add(interaction);
        }
    }

    @Override
    public String toString() {
        return "Tape[" + name + "]";
    }

    private synchronized int findMatch(Request request) {
        final RequestMatcher requestMatcher = new RequestMatcher(request, matchRules);

        return Iterables.indexOf(interactions, new Predicate<RecordedInteraction>() {
            @Override
            public boolean apply(RecordedInteraction input) {
                return requestMatcher.matches(input.getRequest());
            }
        });
    }

    private static RecordedRequest recordRequest(Request request) {
        try {
            final RecordedRequest clone = new RecordedRequest();
            clone.setMethod(request.getMethod());
            clone.setUri(request.getUri());

            for (Map.Entry<String, String> header : request.getHeaders().entrySet()) {
                if (!header.getKey().equals(VIA))
                    clone.getHeaders().put(header.getKey(), header.getValue());
            }

            clone.setBody(request.hasBody() ? CharStreams.toString(request.getBodyAsText()) : null);

            return clone;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /*
    private static RecordedResponse recordResponse(Response response) {
		def clone = new RecordedResponse()
		clone.status = response.status
		response.headers.each {
			if (!(it.key in [VIA, X_BETAMAX])) {
				clone.headers[it.key] = it.value
			}
		}
		if (response.hasBody()) {
			boolean representAsText = isTextContentType(response.contentType) && isPrintable(response.bodyAsText.text)
			clone.body = representAsText ? response.bodyAsText.text : response.bodyAsBinary.bytes
		}
		clone
	}
     */

    private static RecordedResponse recordResponse(Response response) {
        try {
            RecordedResponse clone = new RecordedResponse();
            clone.setStatus(response.getStatus());

            for (Map.Entry<String, String> header : response.getHeaders().entrySet()) {
                if (!header.getKey().equals(VIA) && !header.getKey().equals(X_BETAMAX))
                    clone.getHeaders().put(header.getKey(), header.getValue());
            }

            if (response.hasBody()) {
                boolean representAsText = isTextContentType(response.getContentType()) && isPrintable(CharStreams.toString(response.getBodyAsText()));
                clone.setBody(representAsText ? CharStreams.toString(response.getBodyAsText()) : ByteStreams.toByteArray(response.getBodyAsBinary()));
            }

            return clone;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isTextContentType(String contentType) {
        return contentType != null && Pattern.compile("^text/|application/(json|javascript|(\\w+\\+)?xml)").matcher(contentType).find();
    }

    public static boolean isPrintable(String s) {
        // this check is performed by SnakeYaml but we need to do so *before* unzipping the byte stream otherwise we
        // won't be able to read it back again.
        return !StreamReader.NON_PRINTABLE.matcher(s).find();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<RecordedInteraction> getInteractions() {
        return interactions;
    }

    public void setInteractions(List<RecordedInteraction> interactions) {
        this.interactions = new ArrayList<RecordedInteraction>(interactions);
    }

    private String name;
    public ArrayList<RecordedInteraction> interactions = new ArrayList<RecordedInteraction>();
    private TapeMode mode = READ_WRITE;
    private AtomicInteger orderedIndex = new AtomicInteger();

    @SuppressWarnings({"unchecked"})
    private Comparator<Request>[] matchRules = new Comparator[]{method, uri};

    private static <K, V, Value extends V> Value putAt0(Map<K, V> propOwner, K key, Value value) {
        propOwner.put(key, value);
        return value;
    }
}
