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

package co.freeside.betamax.util.server.internal

import java.security.*
import javax.net.ssl.*
import io.netty.channel.ChannelHandler
import io.netty.channel.socket.SocketChannel
import io.netty.handler.ssl.SslHandler

class HttpsChannelInitializer extends HttpChannelInitializer {

    HttpsChannelInitializer(int workerThreads, ChannelHandler handler) {
        super(workerThreads, handler)
    }

    @Override
    void initChannel(SocketChannel channel) throws Exception {
        super.initChannel(channel)

        def pipeline = channel.pipeline()

        def engine = sslContext().createSSLEngine()
        engine.useClientMode = false
        pipeline.addFirst("ssl", new SslHandler(engine))

    }

    private static SSLContext sslContext() {
        def keystoreStream = HttpsChannelInitializer.getResourceAsStream("/betamax.keystore")
        def password = "password".toCharArray()

        def sslContext = SSLContext.getInstance("TLS")

        def keyStore = KeyStore.getInstance("JKS")
        keyStore.load(keystoreStream, password)

        def algorithm = Security.getProperty("ssl.KeyManagerFactory.algorithm") ?: "SunX509"
        def factory = KeyManagerFactory.getInstance(algorithm)
        factory.init(keyStore, password)

        sslContext.init(factory.keyManagers, null, null)

        return sslContext
    }
}
