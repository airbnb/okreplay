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

package betamax.proxy.jetty

import betamax.Recorder
import betamax.proxy.RecordAndPlaybackProxyInterceptor

class ProxyServer extends SimpleServer {

	private final int timeout

	ProxyServer(int port, int timeout) {
		super(port)
		this.timeout = timeout
	}

	void start(Recorder recorder) {
		def handler = new ProxyHandler()
		handler.interceptor = new RecordAndPlaybackProxyInterceptor(recorder)
		handler.timeout = timeout

		super.start(handler)
	}


}
