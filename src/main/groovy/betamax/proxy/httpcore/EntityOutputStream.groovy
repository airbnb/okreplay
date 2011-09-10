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

import org.apache.http.HttpResponse
import org.apache.http.entity.ByteArrayEntity

class EntityOutputStream extends OutputStream {

	private final HttpResponse response
	private final ByteArrayOutputStream delegate = new ByteArrayOutputStream()

	EntityOutputStream(HttpResponse response) {
		this.response = response
	}

	@Override
	void write(int i) {
		delegate.write(i)
	}

	@Override
	void flush() {
		super.flush()
		flushToEntity()
	}

	@Override
	void close() {
		super.close()
		flushToEntity()
	}

	private void flushToEntity() {
		def entity = new ByteArrayEntity(delegate.toByteArray())
		entity.contentType = response.getFirstHeader(org.apache.http.HttpHeaders.CONTENT_TYPE)
		entity.contentEncoding = response.getFirstHeader(org.apache.http.HttpHeaders.CONTENT_ENCODING)
		response.entity = entity
	}
}
