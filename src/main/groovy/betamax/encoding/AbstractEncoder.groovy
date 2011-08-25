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

package betamax.encoding

abstract class AbstractEncoder {

	final String decode(InputStream input) {
		def out = new StringWriter()
		new InputStreamReader(getDecodingInputStream(input)).withReader { Reader reader ->
			out << reader.text
		}
		out.toString()
	}

	final byte[] encode(String input) {
		def out = new ByteArrayOutputStream()
		getEncodingOutputStream(out).withStream { OutputStream stream ->
			stream << input.bytes
		}
		out.toByteArray()
	}

	protected abstract InputStream getDecodingInputStream(InputStream input)

	protected abstract OutputStream getEncodingOutputStream(OutputStream output)

}
