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

package co.freeside.betamax.util.message

import co.freeside.betamax.proxy.AbstractMessage
import org.apache.commons.lang.StringUtils
import static org.apache.http.HttpHeaders.*

abstract class BasicMessage extends AbstractMessage {

	private Map<String, List<String>> headers = [:]
	byte[] body = new byte[0]

	void addHeader(String name, String value) {
		if (headers.containsKey(name)) {
			headers[name] << value
		} else {
			headers[name] = [value]
		}
	}

	void setHeaders(Map<String, List<String>> headers) {
		this.headers = headers
	}

	Map<String, String> getHeaders() {
		def map = [:]
		for (header in headers) {
			map[header.key] = header.value.join(", ")
		}
		map.asImmutable()
	}

	final boolean hasBody() {
		body
	}

	@Override
	InputStream getBodyAsBinary() {
		new ByteArrayInputStream(body)
	}

	@Override
	String getContentType() {
		StringUtils.substringBefore(getHeader(CONTENT_TYPE), ";")
	}

	@Override
	String getCharset() {
		getHeader(CONTENT_TYPE)?.find(/charset=(.*)/) { match, charset ->
			charset
		}
	}

	@Override
	String getEncoding() {
		getHeader(CONTENT_ENCODING)
	}
}
