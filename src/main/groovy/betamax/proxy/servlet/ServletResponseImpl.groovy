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

package betamax.proxy.servlet

import javax.servlet.http.HttpServletResponse
import betamax.proxy.*

class ServletResponseImpl extends AbstractMessage implements Response {

	private final HttpServletResponse delegate
	private int status
	private final Map<String, List<String>> headers = [:]
	private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream()

	ServletResponseImpl(HttpServletResponse delegate) {
		this.delegate = delegate
	}

	int getStatus() {
		status
	}

	void setStatus(int status) {
		delegate.status = status
		this.status = status
	}

	@Override
	String getContentType() {
		delegate.contentType
	}

	@Override
	String getCharset() {
		delegate.characterEncoding
	}

	@Override
	String getEncoding() {
		headers["Content-Encoding"]?.first()
	}

	String getReason() {
		return null  //To change body of implemented methods use File | Settings | File Templates.
	}

	void setReason(String reason) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	Map<String, List<String>> getHeaders() {
		headers.asImmutable()
	}

	@Override
	protected OutputStream initOutputStream() {
		new OutputStream() {
			@Override
			void write(int b) {
				delegate.outputStream.write(b)
				outputStream.write(b)
			}

			@Override
			void flush() {
				delegate.outputStream.flush()
			}

			@Override
			void close() {
				delegate.outputStream.close()
			}
		}
	}

	void addHeader(String name, String value) {
		if (headers.containsKey(name)) {
			headers[name] << value
		} else {
			headers[name] = [value]
		}

		if (name == "Content-Type") {
			delegate.contentType = value
		} else {
			delegate.addHeader(name, value)
		}
	}

	boolean hasBody() {
		return false  //To change body of implemented methods use File | Settings | File Templates.
	}

	InputStream getBodyAsBinary() {
		new ByteArrayInputStream(outputStream.toByteArray())
	}

}
