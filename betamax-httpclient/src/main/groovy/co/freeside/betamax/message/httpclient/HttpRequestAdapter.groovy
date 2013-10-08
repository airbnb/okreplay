/*
 * Copyright 2012 the original author or authors.
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

package co.freeside.betamax.message.httpclient

import co.freeside.betamax.message.Request
import org.apache.http.*

class HttpRequestAdapter extends HttpMessageAdapter<HttpRequest> implements Request {

	private final HttpRequest delegate

	HttpRequestAdapter(HttpRequest delegate) {
		this.delegate = delegate
	}

	@Override
	protected HttpRequest getDelegate() {
		delegate
	}

	@Override
	String getMethod() {
		delegate.requestLine.method
	}

	@Override
	URI getUri() {
		delegate.requestLine.uri.toURI()
	}

	@Override
	boolean hasBody() {
		delegate instanceof HttpEntityEnclosingRequest
	}

	@Override
	protected InputStream getBodyAsStream() {
		if (delegate instanceof HttpEntityEnclosingRequest) {
			delegate.entity.content
		} else {
			throw new IllegalStateException('no body')
		}
	}
}
