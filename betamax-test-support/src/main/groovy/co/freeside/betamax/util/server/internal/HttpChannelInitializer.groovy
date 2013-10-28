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

import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.*
import io.netty.handler.stream.ChunkedWriteHandler

/**
 * Configures up a channel to handle HTTP requests and responses.
 */
class HttpChannelInitializer extends ChannelInitializer<SocketChannel> {

    public static final int MAX_CONTENT_LENGTH = 65536

    private final ChannelHandler handler
    private final EventLoopGroup workerGroup

    HttpChannelInitializer(int workerThreads, ChannelHandler handler) {
        this.handler = handler

        if (workerThreads > 0) {
            workerGroup = new NioEventLoopGroup(workerThreads)
        } else {
            workerGroup = null
        }
    }

    @Override
    void initChannel(SocketChannel channel) {
        def pipeline = channel.pipeline()
        pipeline.addLast("decoder", new HttpRequestDecoder())
        pipeline.addLast("aggregator", new HttpObjectAggregator(MAX_CONTENT_LENGTH))
        pipeline.addLast("encoder", new HttpResponseEncoder())
        pipeline.addLast("chunkedWriter", new ChunkedWriteHandler())
        if (workerGroup) {
            pipeline.addLast(workerGroup, "handler", handler)
        } else {
            pipeline.addLast("handler", handler)
        }
    }
}
