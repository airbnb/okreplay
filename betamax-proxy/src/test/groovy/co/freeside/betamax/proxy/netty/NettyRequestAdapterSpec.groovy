/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package co.freeside.betamax.proxy.netty

import io.netty.handler.codec.http.HttpRequest
import static io.netty.handler.codec.http.HttpMethod.GET

class NettyRequestAdapterSpec extends NettyMessageAdapterSpec<HttpRequest, NettyRequestAdapter> {

    void setup() {
        nettyMessage = Mock(HttpRequest) {
            headers() >> nettyMessageHeaders
        }
    }

    @Override
    void createAdapter() {
        adapter = new NettyRequestAdapter(nettyMessage)
    }

	void 'can read basic fields'() {
		given:
		nettyMessage.method >> GET
		nettyMessage.uri >> 'http://freeside.co/betamax'

        and:
        createAdapter()

		expect:
		adapter.method == 'GET'
		adapter.uri == 'http://freeside.co/betamax'.toURI()
	}

	void 'uri includes query string'() {
		given:
		nettyMessage.uri >> 'http://freeside.co/betamax?q=1'

        and:
        createAdapter()

        expect:
		adapter.uri == new URI('http://freeside.co/betamax?q=1')
	}
}
