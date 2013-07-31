package co.freeside.betamax.proxy.netty;

import io.netty.channel.*;
import io.netty.channel.nio.*;
import io.netty.channel.socket.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.*;

/**
 * Configures up a channel to handle HTTP requests and responses.
 */
public class HttpChannelInitializer extends ChannelInitializer<SocketChannel> {

    public static final int MAX_CONTENT_LENGTH = 65536;

    private final int workerThreads;
    private final ChannelHandler handler;
    private final EventLoopGroup workerGroup;

    public HttpChannelInitializer(int workerThreads, ChannelHandler handler) {
        this.workerThreads = workerThreads;
        this.handler = handler;

        if (workerThreads > 0) {
            workerGroup = new NioEventLoopGroup(workerThreads);
        } else {
            workerGroup = null;
        }
    }

    @Override
    public void initChannel(SocketChannel channel) throws Exception {
        ChannelPipeline pipeline = channel.pipeline();
        pipeline
                .addLast(new HttpRequestDecoder())
                .addLast(new HttpObjectAggregator(MAX_CONTENT_LENGTH))
                .addLast(new HttpResponseEncoder())
                .addLast(new ChunkedWriteHandler());
        if (workerGroup == null) {
            pipeline.addLast(handler);
        } else {
            pipeline.addLast(workerGroup, handler);
        }
    }
}
