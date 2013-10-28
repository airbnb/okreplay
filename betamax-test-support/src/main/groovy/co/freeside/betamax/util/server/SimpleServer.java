package co.freeside.betamax.util.server;

import java.net.*;
import io.netty.bootstrap.*;
import io.netty.channel.*;
import io.netty.channel.nio.*;
import io.netty.channel.socket.nio.*;

public class SimpleServer {

    public static final int DEFAULT_PORT = 5000;

    private final int port;
    private final ChannelInitializer channelInitializer;
    private EventLoopGroup group;
    private Channel channel;

    public SimpleServer(int port, ChannelHandler handler) {
        this.port = port;
        this.channelInitializer = createChannelInitializer(handler);
    }

    public SimpleServer(ChannelHandler handler) {
        this(DEFAULT_PORT, handler);
    }

    public SimpleServer(int port, Class<? extends ChannelHandler> handlerType) throws IllegalAccessException, InstantiationException {
        this(port, handlerType.newInstance());
    }

    public SimpleServer(Class<? extends ChannelHandler> handlerType) throws InstantiationException, IllegalAccessException {
        this(DEFAULT_PORT, handlerType);
    }

    public static SimpleServer start(Class<? extends ChannelHandler> handlerType) throws IllegalAccessException, InstantiationException, InterruptedException {
        SimpleServer server = new SimpleServer(handlerType);
        server.start();
        return server;
    }

    public static SimpleServer start(ChannelHandler handler) throws InterruptedException {
        SimpleServer server = new SimpleServer(handler);
        server.start();
        return server;
    }

    protected ChannelInitializer createChannelInitializer(ChannelHandler handler) {
        return new HttpChannelInitializer(0, handler);
    }

    protected String getUrlScheme() {
        return "http";
    }

    public String getUrl() {
        return String.format("%s://localhost:%s/", getUrlScheme(), port);
    }

    public InetSocketAddress start() throws InterruptedException {
        group = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(group)
                .channel(NioServerSocketChannel.class)
                .childHandler(channelInitializer)
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);

        channel = bootstrap.bind("localhost", port).sync().channel();
        return (InetSocketAddress) channel.localAddress();
    }

    public void stop() throws InterruptedException {
        if (channel != null) {
            channel.close().sync();
        }
        if (group != null) {
            group.shutdownGracefully().sync();
        }
    }
}
