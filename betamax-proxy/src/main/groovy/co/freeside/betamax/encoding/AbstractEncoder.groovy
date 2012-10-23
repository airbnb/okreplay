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

package co.freeside.betamax.encoding

import java.nio.charset.Charset

abstract class AbstractEncoder {

	final String decode(InputStream input, String charset = Charset.defaultCharset().toString()) {
		new InputStreamReader(getDecodingInputStream(input), charset).text
	}

	final byte[] encode(String input, String charset = Charset.defaultCharset().toString()) {
		def out = new ByteArrayOutputStream()
		getEncodingOutputStream(out).withStream { OutputStream stream ->
			stream << input.getBytes(charset)
		}
		out.toByteArray()
	}

	protected abstract InputStream getDecodingInputStream(InputStream input)

	protected abstract OutputStream getEncodingOutputStream(OutputStream output)

}
