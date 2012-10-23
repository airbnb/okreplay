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

package co.freeside.betamax.message

import java.util.zip.*
import org.apache.commons.lang.StringUtils
import static org.apache.http.HttpHeaders.*

abstract class AbstractMessage implements Message {

	public static final String DEFAULT_CONTENT_TYPE = 'application/octet-stream'
	public static final String DEFAULT_CHARSET = 'UTF-8'
	public static final String DEFAULT_ENCODING = 'none'

	String getContentType() {
		def contentTypeHeader = getHeader(CONTENT_TYPE)
		if (contentTypeHeader) {
			StringUtils.substringBefore(contentTypeHeader, ';')
		} else {
			DEFAULT_CONTENT_TYPE
		}
	}

	String getCharset() {
		def declaredCharset = getHeader(CONTENT_TYPE)?.find(/charset=(.*)/) { match, charset ->
			charset
		}
		declaredCharset ?: DEFAULT_CHARSET
	}

	String getEncoding() {
		getHeader(CONTENT_ENCODING) ?: DEFAULT_ENCODING
	}

	String getHeader(String name) {
		headers[name]
	}

	/**
	 * A default implementation that decodes the byte stream from `getBodyAsBinary`. Implementations can override this
	 * if they have a simpler way of doing it.
	 */
	Reader getBodyAsText() {
		def stream
		switch (encoding) {
			case 'gzip': stream = new GZIPInputStream(bodyAsBinary); break
			case 'deflate': stream = new InflaterInputStream(bodyAsBinary); break
			default: stream = bodyAsBinary
		}
		charset ? new InputStreamReader(stream, charset) : new InputStreamReader(stream)
	}

}
