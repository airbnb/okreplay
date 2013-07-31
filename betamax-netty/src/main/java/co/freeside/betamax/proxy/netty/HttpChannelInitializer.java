package co.freeside.betamax.proxy.netty;

import io.netty.channel.*;
import io.netty.channel.socket.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.*;

/**
 * Configures up a channel to handle HTTP requests and responses.
 */
public class HttpChannelInitializer extends ChannelInitializer<SocketChannel> {

    public static final int MAX_CONTENT_LENGTH = 65536;

    private final ChannelHandler handler;

    public HttpChannelInitializer(ChannelHandler handler) {
        this.handler = handler;
    }

    @Override
    public void initChannel(SocketChannel channel) throws Exception {
        channel.pipeline()
                .addLast(new HttpRequestDecoder())
                .addLast(new HttpObjectAggregator(MAX_CONTENT_LENGTH))
                .addLast(new HttpResponseEncoder())
                .addLast(new ChunkedWriteHandler())
                .addLast(handler);
    }
}
