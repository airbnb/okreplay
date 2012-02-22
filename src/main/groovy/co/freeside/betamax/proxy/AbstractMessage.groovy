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

package co.freeside.betamax.proxy

import java.util.zip.*

abstract class AbstractMessage implements Message {

	private OutputStream outputStream
	private Writer writer

	abstract String getContentType()

	abstract String getCharset()

	abstract String getEncoding()

	protected abstract OutputStream initOutputStream()

	final String getHeader(String name) {
		headers[name]
	}

	final Reader getBodyAsText() {
		def stream
		switch (encoding) {
			case "gzip": stream = new GZIPInputStream(bodyAsBinary); break
			case "deflate": stream = new InflaterInputStream(bodyAsBinary); break
			default: stream = bodyAsBinary
		}
		charset ? new InputStreamReader(stream, charset) : new InputStreamReader(stream)
	}

	final OutputStream getOutputStream() {
		if (!outputStream) {
			outputStream = initOutputStream()
		}
		outputStream
	}

	final Writer getWriter() {
		if (!writer) {
			writer = initWriter()
		}
		writer
	}

	private Writer initWriter() {
		if (!outputStream) {
			outputStream = initOutputStream()
		}
		def stream
		switch (encoding) {
			case "gzip": stream = new GZIPOutputStream(outputStream); break
			case "deflate": stream = new DeflaterOutputStream(outputStream); break
			default: stream = outputStream
		}
		charset ? new OutputStreamWriter(stream, charset) : new OutputStreamWriter(stream)
	}

}
