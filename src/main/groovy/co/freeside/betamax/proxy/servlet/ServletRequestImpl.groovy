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

package co.freeside.betamax.proxy.servlet

import javax.servlet.http.HttpServletRequest

import co.freeside.betamax.proxy.Request
import co.freeside.betamax.proxy.AbstractMessage

class ServletRequestImpl extends AbstractMessage implements Request {

	private final HttpServletRequest delegate
	private final Map<String, String> headers = [:]

	ServletRequestImpl(HttpServletRequest delegate) {
		this.delegate = delegate
	}

	String getMethod() {
		delegate.method
	}

	@Override
	String getContentType() {
		return null  //To change body of implemented methods use File | Settings | File Templates.
	}

	URI getUri() {
		def uri = delegate.requestURL
		def qs = delegate.queryString
		if (qs) {
			uri << "?" << qs
		}
		uri.toString().toURI()
	}

	@Override
	String getCharset() {
		return null  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	String getEncoding() {
		return null  //To change body of implemented methods use File | Settings | File Templates.
	}

	Map<String, String> getHeaders() {
		if (headers.isEmpty()) {
			for (headerName in delegate.headerNames) {
				headers[headerName] = delegate.getHeaders(headerName).toList().join(", ")
			}
		}
		headers.asImmutable()
	}

	@Override
	protected OutputStream initOutputStream() {
		return null  //To change body of implemented methods use File | Settings | File Templates.
	}

	boolean hasBody() {
		delegate.contentLength > 0
	}

	InputStream getBodyAsBinary() {
		delegate.inputStream
	}


}
