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

package betamax.proxy.httpcore

import betamax.proxy.Request
import org.apache.http.*

class HttpCoreRequestImpl extends HttpCoreMessageImpl<HttpRequest> implements Request {

	private final boolean hasEntity

	HttpCoreRequestImpl(HttpRequest delegate) {
		super(delegate)
		this.hasEntity = delegate instanceof HttpEntityEnclosingRequest
	}

	String getMethod() {
		delegate.requestLine.method
	}

	URI getTarget() {
		new URI(delegate.requestLine.uri)
	}

	boolean hasBody() {
		delegate instanceof HttpEntityEnclosingRequest
	}

	// TODO: this should not be in the API but I need multiple inheritance otherwise
	@Override
	protected OutputStream initOutputStream() {
		throw new UnsupportedOperationException()
	}

}
