/*
 * Copyright 2011 Rob Fletcher
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

import co.freeside.betamax.message.AbstractMessage
import co.freeside.betamax.message.Request

import javax.servlet.http.HttpServletRequest

class ServletRequestAdapter extends AbstractMessage implements Request {

	private final HttpServletRequest delegate
	private byte[] body

	ServletRequestAdapter(HttpServletRequest delegate) {
		this.delegate = delegate
	}

	@Override
	String getMethod() {
		delegate.method
	}

	@Override
	URI getUri() {
		def uri = delegate.requestURL
		def qs = delegate.queryString
		if (qs) {
			uri << '?' << qs
		}
		uri.toString().toURI()
	}

	@Override
	String getCharset() {
		delegate.characterEncoding
	}

	@Override
	Map<String, String> getHeaders() {
        	delegate.headerNames.toList().inject([:]) { map, header ->
            		map << new MapEntry(header, getHeader(header))
        	}.asImmutable()
	}

	@Override
	String getHeader(String name) {
		delegate.getHeaders(name).toList().join(', ')
	}

	@Override
	void addHeader(String name, String value) {
		throw new UnsupportedOperationException()
	}

	@Override
	boolean hasBody() {
		delegate.contentLength > 0
	}

	@Override
	InputStream getBodyAsBinary() {
		if (!body) {
			body = delegate.inputStream.bytes
		}
		new ByteArrayInputStream(body)
	}

	HttpServletRequest getOriginalRequest() {
		delegate
	}

}
