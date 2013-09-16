/*
 * Copyright 2013 Rob Fletcher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package co.freeside.betamax.proxy.netty;

import java.io.*;
import java.net.*;
import java.util.*;
import co.freeside.betamax.message.*;
import com.google.common.base.*;
import io.netty.handler.codec.http.*;

public class NettyRequestAdapter extends AbstractMessage implements Request {

	private final FullHttpRequest delegate;
	private byte[] body;

    public static Request wrap(HttpObject message) {
        if (message instanceof FullHttpRequest) {
            return new NettyRequestAdapter((FullHttpRequest) message);
        } else {
            throw new IllegalArgumentException(String.format("%s is not an instance of %s", message.getClass().getName(), FullHttpRequest.class.getName()));
        }
    }

	NettyRequestAdapter(FullHttpRequest delegate) {
		this.delegate = delegate;
	}

	@Override
	public String getMethod() {
		return delegate.getMethod().name();
	}

	@Override
	public URI getUri() {
		try {
			return new URI(delegate.getUri());
		} catch (URISyntaxException e) {
			throw new IllegalStateException("Invalid URI in underlying request", e);
		}
	}

	@Override
	public Map<String, String> getHeaders() {
		Map<String, String> headers = new HashMap<String, String>();
		for (String name : delegate.headers().names()) {
			headers.put(name, getHeader(name));
		}
		return Collections.unmodifiableMap(headers);
	}

	@Override
	public String getHeader(String name) {
		return Joiner.on(", ").join(delegate.headers().getAll(name));
	}

	@Override
	public void addHeader(String name, String value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasBody() {
		return delegate.content() != null && delegate.content().isReadable();
	}

	@Override
	public InputStream getBodyAsBinary() throws IOException {
		// TODO: can this be done without copying the entire byte array?
		if (body == null) {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			delegate.content().getBytes(0, stream, delegate.content().readableBytes());
			body = stream.toByteArray();
		}
		return new ByteArrayInputStream(body);
	}

	public final FullHttpRequest getOriginalRequest() {
		return delegate;
	}

}
