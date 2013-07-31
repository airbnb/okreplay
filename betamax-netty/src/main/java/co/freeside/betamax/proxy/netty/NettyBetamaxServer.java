package co.freeside.betamax.proxy.netty;

import java.net.*;
import io.netty.bootstrap.*;
import io.netty.channel.*;
import io.netty.channel.nio.*;
import io.netty.channel.socket.nio.*;

/**
 * A Netty-based implementation of the Betamax proxy server.
 */
public class NettyBetamaxServer {

    private final int port;
    private final ChannelHandler handler;
    private EventLoopGroup group;
    private Channel channel;

    public NettyBetamaxServer(int port, ChannelHandler handler) {
        this.port = port;
        this.handler = handler;
    }

    public InetSocketAddress run() throws Exception {
        group = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(group)
                .channel(NioServerSocketChannel.class)
                .childHandler(new HttpChannelInitializer(handler))
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);

        channel = bootstrap.bind(port).sync().channel();
        return (InetSocketAddress) channel.localAddress();
    }

    public void shutdown() throws InterruptedException {
        if (channel != null) channel.close().sync();
        if (group != null) group.shutdownGracefully();
    }

}