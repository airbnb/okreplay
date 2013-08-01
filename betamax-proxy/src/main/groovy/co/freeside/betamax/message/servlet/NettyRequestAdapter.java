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

package co.freeside.betamax.message.servlet

import co.freeside.betamax.message.*
import io.netty.handler.codec.http.FullHttpRequest

class NettyRequestAdapter extends AbstractMessage implements Request {

	private final FullHttpRequest delegate
	private byte[] body

	NettyRequestAdapter(FullHttpRequest delegate) {
		this.delegate = delegate
	}

	@Override
	String getMethod() {
		delegate.method
	}

	@Override
	URI getUri() {
		delegate.uri.toURI()
	}

	@Override
	Map<String, String> getHeaders() {
		delegate.headers().names().inject([:]) { map, header ->
			map << new MapEntry(header, getHeader(header))
		}.asImmutable()
	}

	@Override
	String getHeader(String name) {
		delegate.headers().getAll(name).join(", ")
	}

	@Override
	void addHeader(String name, String value) {
		throw new UnsupportedOperationException()
	}

	@Override
	boolean hasBody() {
		delegate.content().readable
	}

	@Override
	InputStream getBodyAsBinary() {
		// TODO: can this be done without copying the entire byte array?
		if (!body) {
			def stream = new ByteArrayOutputStream()
			delegate.content().getBytes(0, stream, delegate.content().readableBytes())
			body = stream.toByteArray()
		}
		new ByteArrayInputStream(body)
	}

	final FullHttpRequest getOriginalRequest() {
		delegate
	}

}
