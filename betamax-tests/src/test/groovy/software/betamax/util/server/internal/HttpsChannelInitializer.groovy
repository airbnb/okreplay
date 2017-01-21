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

package software.betamax.util.server.internal

import com.google.common.io.Files
import io.netty.channel.ChannelHandler
import io.netty.channel.socket.SocketChannel
import io.netty.handler.ssl.SslHandler
import org.apache.commons.io.FileUtils
import org.littleshoot.proxy.MitmManager
import org.littleshoot.proxy.mitm.Authority
import org.littleshoot.proxy.mitm.CertificateSniffingMitmManager

import java.nio.file.Path

class HttpsChannelInitializer extends HttpChannelInitializer {

    HttpsChannelInitializer(int workerThreads, ChannelHandler handler) {
        super(workerThreads, handler)
    }

    @Override
    void initChannel(SocketChannel channel) throws Exception {
        super.initChannel(channel)

        def pipeline = channel.pipeline()

        // Use the same betamax private key & cert for backwards compatibility with 2.0.1
        // We use temporary files here so we don't pollute people's projects with temporary (and fake) keys and certs
        File tempSSLDir = Files.createTempDir();

        Path storePath = tempSSLDir.toPath().resolve("betamax.p12");
        InputStream betamaxKeystoreStream = getClass().getClassLoader().getResourceAsStream("betamax.p12");
        FileUtils.copyInputStreamToFile(betamaxKeystoreStream, storePath.toFile());

        Path keyPath = tempSSLDir.toPath().resolve("betamax.pem");
        InputStream betamaxKeyStream = getClass().getClassLoader().getResourceAsStream("betamax.pem");
        FileUtils.copyInputStreamToFile(betamaxKeyStream, keyPath.toFile());

        def hostName = channel.localAddress().getHostName()

        Authority authority = new Authority(
                tempSSLDir,
                hostName,
                "changeit".toCharArray(),
                hostName,
                "Betamax",
                "Certificate Authority",
                "Betamax",
                "betamax.software"
        );

        MitmManager manager = new CertificateSniffingMitmManager(authority);

        def engine = manager.serverSslEngine()
        engine.useClientMode = false
        pipeline.addFirst("ssl", new SslHandler(engine))
    }
}
