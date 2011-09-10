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

import betamax.proxy.AbstractMessage
import org.apache.http.HttpMessage
import static org.apache.http.HttpHeaders.*

abstract class HttpCoreMessageImpl<T extends HttpMessage> extends AbstractMessage {

	private final T delegate
	private InputStream inputStream

	HttpCoreMessageImpl(T delegate) {
		this.delegate = delegate
	}

	protected T getDelegate() {
		delegate
	}

	final Map<String, List<String>> getHeaders() {
		def map = [:]
		delegate.allHeaders.each {
			if (map.containsKey(it.name)) {
				map[it.name] << it.value
			} else {
				map[it.name] = [it.value]
			}
		}
		map.asImmutable()
	}

	final void addHeader(String name, String value) {
		delegate.addHeader(name, value)
	}

	final InputStream getBodyAsBinary() {
		if (inputStream) {
			inputStream.reset()
		} else {
			if (hasBody()) {
				if (delegate.entity.isRepeatable()) {
					inputStream = delegate.entity.content
				} else {
					inputStream = new ByteArrayInputStream(delegate.entity.content.bytes)
				}
			} else {
				throw new UnsupportedOperationException("no body")
			}
		}
		inputStream
	}

	@Override
	final String getContentType() {
		def contentType = null
		def contentTypeHeader = delegate.getFirstHeader(CONTENT_TYPE)
		if (contentTypeHeader) {
			def values = contentTypeHeader?.elements
			if (values.length > 0) {
				contentType = values[0].name
			}
		}
		contentType
	}

	@Override
	final String getCharset() {
		def charset = null
		def contentTypeHeader = delegate.getFirstHeader(CONTENT_TYPE)
		if (contentTypeHeader) {
			def values = contentTypeHeader.elements
			if (values.length > 0) {
				def param = values[0].getParameterByName("charset")
				if (param) {
					charset = param.value
				}
			}
		}
		charset
	}

	@Override
	final String getEncoding() {
		delegate.getFirstHeader(CONTENT_ENCODING)?.value
	}
}
