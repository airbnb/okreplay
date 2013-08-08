package co.freeside.betamax.proxy.netty;

import javax.net.ssl.*;
import co.freeside.betamax.proxy.netty.ssl.*;
import io.netty.channel.*;
import io.netty.channel.nio.*;
import io.netty.channel.socket.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.*;
import io.netty.handler.stream.*;

/**
 * Configures up a channel to handle HTTP requests and responses.
 */
public class HttpChannelInitializer extends ChannelInitializer<SocketChannel> {

    public static final int MAX_CONTENT_LENGTH = 65536;

    private final ChannelHandler handler;
    private final EventLoopGroup workerGroup;

    public HttpChannelInitializer(int workerThreads, ChannelHandler handler) {
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

//        SSLEngine engine = SslContextFactory.getServerContext().createSSLEngine();
//        engine.setUseClientMode(false);

//        pipeline.addLast("ssl", new SslHandler(engine));
        pipeline.addLast("decoder", new HttpRequestDecoder());
        pipeline.addLast("aggregator", new HttpObjectAggregator(MAX_CONTENT_LENGTH));
        pipeline.addLast("encoder", new HttpResponseEncoder());
        pipeline.addLast("chunkedWriter", new ChunkedWriteHandler());
        if (workerGroup == null) {
            pipeline.addLast("betamaxHandler", handler);
        } else {
            pipeline.addLast(workerGroup, "betamaxHandler", handler);
        }
    }
}
