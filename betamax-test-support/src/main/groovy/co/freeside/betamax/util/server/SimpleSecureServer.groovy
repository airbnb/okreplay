/*
 * Copyright 2011 the original author or authors.
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

package co.freeside.betamax.util.server

import co.freeside.betamax.util.server.internal.HttpsChannelInitializer
import groovy.transform.*
import io.netty.channel.*

@InheritConstructors
class SimpleSecureServer extends SimpleServer {

    static SimpleSecureServer start(Class<? extends ChannelHandler> handlerType) {
        def server = new SimpleSecureServer(handlerType)
        server.start()
        return server
    }

    static SimpleSecureServer start(ChannelHandler handler) {
        def server = new SimpleSecureServer(handler)
        server.start()
        return server
    }

    @Override
    protected ChannelInitializer createChannelInitializer(ChannelHandler handler) {
        new HttpsChannelInitializer(0, handler)
    }

    protected String getUrlScheme() {
        "https"
    }

}
