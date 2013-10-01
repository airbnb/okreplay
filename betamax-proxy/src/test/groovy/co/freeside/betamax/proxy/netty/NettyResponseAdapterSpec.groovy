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

import io.netty.handler.codec.http.*

class NettyResponseAdapterSpec extends NettyMessageAdapterSpec<HttpResponse, NettyResponseAdapter> {

	void setup() {
		nettyMessage = Mock(HttpResponse) {
            headers() >> nettyMessageHeaders
        }
    }

    @Override
    void createAdapter() {
		adapter = new NettyResponseAdapter(nettyMessage)
	}

	void "can read response status"() {
		given:
		nettyMessage.status >> HttpResponseStatus.CREATED

        and:
        createAdapter()

        expect:
		adapter.status == HttpResponseStatus.CREATED.code()
	}

}

