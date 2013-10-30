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

package co.freeside.betamax.util.server

import co.freeside.betamax.util.server.internal.HttpChannelInitializer
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel

class SimpleServer {

    static final int DEFAULT_PORT = 5000

    private final int port
    private final ChannelInitializer channelInitializer
    private EventLoopGroup group
    private Channel channel

    SimpleServer(int port, ChannelHandler handler) {
        this.port = port
        this.channelInitializer = createChannelInitializer(handler)
    }

    SimpleServer(ChannelHandler handler) {
        this(DEFAULT_PORT, handler)
    }

    SimpleServer(int port, Class<? extends ChannelHandler> handlerType) {
        this(port, handlerType.newInstance())
    }

    SimpleServer(Class<? extends ChannelHandler> handlerType) {
        this(DEFAULT_PORT, handlerType)
    }

    static SimpleServer start(Class<? extends ChannelHandler> handlerType) {
        SimpleServer server = new SimpleServer(handlerType)
        server.start()
        return server
    }

    static SimpleServer start(ChannelHandler handler) {
        SimpleServer server = new SimpleServer(handler)
        server.start()
        return server
    }

    protected ChannelInitializer createChannelInitializer(ChannelHandler handler) {
        return new HttpChannelInitializer(0, handler)
    }

    protected String getUrlScheme() {
        return "http"
    }

    String getUrl() {
        "$urlScheme://localhost:$port/"
    }

    int getPort() {
        port
    }

    InetSocketAddress start() {
        group = new NioEventLoopGroup()
        def bootstrap = new ServerBootstrap()
        bootstrap.group(group)
                .channel(NioServerSocketChannel)
                .childHandler(channelInitializer)
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true)

        channel = bootstrap.bind("localhost", port).sync().channel()
        return (InetSocketAddress) channel.localAddress()
    }

    void stop() {
        channel?.close()?.sync()
        group?.shutdownGracefully()?.sync()
    }
}
