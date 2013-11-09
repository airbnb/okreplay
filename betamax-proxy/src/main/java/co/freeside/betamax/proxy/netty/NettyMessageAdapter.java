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

package co.freeside.betamax.proxy.netty;

import java.io.*;
import java.util.Map;
import co.freeside.betamax.message.AbstractMessage;
import com.google.common.base.Joiner;
import com.google.common.collect.*;
import com.google.common.io.ByteStreams;
import io.netty.buffer.*;
import io.netty.handler.codec.http.*;

public abstract class NettyMessageAdapter<T extends HttpMessage> extends AbstractMessage {

    protected final T delegate;
    private final Multimap<String, String> headers = LinkedHashMultimap.create();
    private final ByteArrayOutputStream body = new ByteArrayOutputStream();

    protected NettyMessageAdapter(T delegate) {
        this.delegate = delegate;
        copyHeaders(delegate);
    }

    /**
     * LittleProxy will use multiple request / response objects and sometimes
     * subsequent ones will contain additional headers.
     */
    public void copyHeaders(HttpMessage httpMessage) {
        for (String name : httpMessage.headers().names()) {
            for (String value : httpMessage.headers().getAll(name)) {
                if (!headers.containsEntry(name, value)) {
                    headers.put(name, value);
                }
            }
        }
    }

    public void append(HttpContent chunk) throws IOException {
        ByteBuf buffer = chunk.content();
        InputStream from = new ByteBufInputStream(buffer);
        try {
            ByteStreams.copy(from, body);
        } finally {
            from.close();
            buffer.resetReaderIndex();
        }
    }

    @Override
    public Map<String, String> getHeaders() {
        ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
        for (String name : headers.keySet()) {
            builder.put(name, getHeader(name));
        }
        return builder.build();
    }

    @Override
    public String getHeader(String name) {
        return Joiner.on(", ").join(headers.get(name));
    }

    @Override
    public void addHeader(String name, String value) {
        headers.put(name, value);
    }

    @Override
    public boolean hasBody() {
        return body.size() > 0;
    }

    @Override
    protected InputStream getBodyAsStream() throws IOException {
        // TODO: can this be done without copying the entire byte array?
        return new ByteArrayInputStream(body.toByteArray());
    }
}
