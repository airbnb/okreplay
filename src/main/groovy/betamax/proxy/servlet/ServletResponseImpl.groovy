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

	ServletResponseImpl(HttpServletResponse delegate) {
		this.delegate = delegate
	}

	int getStatus() {
		delegate.status
	}

	void setStatus(int status) {
		delegate.status = status
	}

	@Override
	String getContentType() {
		return null  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	String getCharset() {
		return null  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	String getEncoding() {
		return null  //To change body of implemented methods use File | Settings | File Templates.
	}

	String getReason() {
		return null  //To change body of implemented methods use File | Settings | File Templates.
	}

	Map<String, List<String>> getHeaders() {
		return null  //To change body of implemented methods use File | Settings | File Templates.
	}

	void setReason(String reason) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	protected OutputStream initOutputStream() {
		return null  //To change body of implemented methods use File | Settings | File Templates.
	}

	void addHeader(String name, String value) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	boolean hasBody() {
		return false  //To change body of implemented methods use File | Settings | File Templates.
	}

	InputStream getBodyAsBinary() {
		return null  //To change body of implemented methods use File | Settings | File Templates.
	}

}
