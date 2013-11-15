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

package co.freeside.betamax.tape;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import co.freeside.betamax.*;
import co.freeside.betamax.handler.NonWritableTapeException;
import co.freeside.betamax.io.*;
import co.freeside.betamax.message.*;
import co.freeside.betamax.message.tape.*;
import com.google.common.base.Predicate;
import com.google.common.collect.*;
import com.google.common.io.*;
import static co.freeside.betamax.Headers.X_BETAMAX;
import static com.google.common.net.HttpHeaders.VIA;
import static java.util.Collections.unmodifiableList;

/**
 * Represents a set of recorded HTTP interactions that can be played back or
 * appended to.
 */
public abstract class MemoryTape implements Tape {

    private String name;
    private List<RecordedInteraction> interactions = Lists.newArrayList();

    private transient TapeMode mode = Configuration.DEFAULT_MODE;
    private transient MatchRule matchRule = Configuration.DEFAULT_MATCH_RULE;
    private transient EntityStorage responseBodyStorage = Configuration.DEFAULT_RESPONSE_BODY_STORAGE;
    private final transient FileResolver fileResolver;

    private transient AtomicInteger orderedIndex = new AtomicInteger();

    protected MemoryTape(FileResolver fileResolver) {
        this.fileResolver = fileResolver;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public TapeMode getMode() {
        return mode;
    }

    @Override
    public void setMode(TapeMode mode) {
        this.mode = mode;
    }

    @Override
    public void setMatchRule(MatchRule matchRule) {
        this.matchRule = matchRule;
    }

    @Override
    public void setResponseBodyStorage(EntityStorage responseBodyStorage) {
        this.responseBodyStorage = responseBodyStorage;
    }

    @Override
    public boolean isReadable() {
        return mode.isReadable();
    }

    @Override
    public boolean isWritable() {
        return mode.isWritable();
    }

    @Override
    public boolean isSequential() {
        return mode.isSequential();
    }

    @Override
    public int size() {
        return interactions.size();
    }

    public List<RecordedInteraction> getInteractions() {
        return unmodifiableList(interactions);
    }

    public void setInteractions(List<RecordedInteraction> interactions) {
        this.interactions = Lists.newArrayList(interactions);
    }

    @Override
    public boolean seek(Request request) {
        if (isSequential()) {
            try {
                // TODO: it's a complete waste of time using an AtomicInteger when this method is called before play in a non-transactional way
                Integer index = orderedIndex.get();
                RecordedInteraction interaction = interactions.get(index);
                RecordedRequest nextRequest = interaction == null ? null : interaction.getRequest();
                return nextRequest != null && matchRule.isMatch(request, nextRequest);
            } catch (IndexOutOfBoundsException e) {
                throw new NonWritableTapeException();
            }
        } else {
            return findMatch(request) >= 0;
        }
    }

    @Override
    public Response play(final Request request) {
        if (!mode.isReadable()) {
            throw new IllegalStateException("the tape is not readable");
        }

        if (mode.isSequential()) {
            Integer nextIndex = orderedIndex.getAndIncrement();
            final RecordedInteraction nextInteraction = interactions.get(nextIndex);
            if (nextInteraction == null) {
                throw new IllegalStateException(String.format("No recording found at position %s", nextIndex));
            }

            if (!matchRule.isMatch(request, nextInteraction.getRequest())) {
                throw new IllegalStateException(String.format("Request %s does not match recorded request %s", stringify(request), stringify(nextInteraction.getRequest())));
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
        try {
            return "method: " + request.getMethod() + ", "
                    + "uri: " + request.getUri() + ", "
                    + "headers: " + request.getHeaders() + ", "
                    + "body: " + CharStreams.toString(request.getBodyAsText());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized void record(Request request, Response response) {
        if (!mode.isWritable()) {
            throw new IllegalStateException("the tape is not writable");
        }

        RecordedInteraction interaction = new RecordedInteraction();

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

        interaction.setRequest(recordRequest(request));
        interaction.setResponse(recordResponse(response));
        interaction.setRecorded(new Date());
    }

    @Override
    public String toString() {
        return String.format("Tape[%s]", name);
    }

    private synchronized int findMatch(final Request request) {
        return Iterables.indexOf(interactions, new Predicate<RecordedInteraction>() {
            @Override
            public boolean apply(RecordedInteraction input) {
                return matchRule.isMatch(request, input.getRequest());
            }
        });
    }

    private static RecordedRequest recordRequest(Request request) {
        try {
            final RecordedRequest recording = new RecordedRequest();
            recording.setMethod(request.getMethod());
            recording.setUri(request.getUri());

            for (Map.Entry<String, String> header : request.getHeaders().entrySet()) {
                if (!header.getKey().equals(VIA)) {
                    recording.getHeaders().put(header.getKey(), header.getValue());
                }
            }

            if (request.hasBody()) {
                recording.setBody(CharStreams.toString(request.getBodyAsText()));
            }

            return recording;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private RecordedResponse recordResponse(Response response) {
        try {
            RecordedResponse recording = new RecordedResponse();
            recording.setStatus(response.getStatus());

            for (Map.Entry<String, String> header : response.getHeaders().entrySet()) {
                if (!header.getKey().equals(VIA) && !header.getKey().equals(X_BETAMAX)) {
                    recording.getHeaders().put(header.getKey(), header.getValue());
                }
            }

            if (response.hasBody()) {
                recordResponseBody(response, recording);
            }

            return recording;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void recordResponseBody(Response response, RecordedResponse recording) throws IOException {
        switch (responseBodyStorage) {
            case external:
                recordBodyToFile(response, recording);
                break;
            default:
                recordBodyInline(response, recording);
        }
    }

    private void recordBodyInline(Message message, RecordedMessage recording) throws IOException {
        boolean representAsText = isTextContentType(message.getContentType());
        if (representAsText) {
            recording.setBody(CharStreams.toString(message.getBodyAsText()));
        } else {
            recording.setBody(ByteStreams.toByteArray(message.getBodyAsBinary()));
        }
    }

    private void recordBodyToFile(Message message, RecordedMessage recording) throws IOException {
        String filename = FileTypeMapper.filenameFor(String.format("response-%02d", size() + 1), message.getContentType());
        File body = fileResolver.toFile(FilenameNormalizer.toFilename(name), filename);
        Files.createParentDirs(body);
        ByteStreams.copy(message.getBodyAsBinary(), Files.newOutputStreamSupplier(body));
        recording.setBody(body);
    }

    public static boolean isTextContentType(String contentType) {
        return contentType != null && Pattern.compile("^text/|application/(json|javascript|(\\w+\\+)?xml)").matcher(contentType).find();
    }

}
