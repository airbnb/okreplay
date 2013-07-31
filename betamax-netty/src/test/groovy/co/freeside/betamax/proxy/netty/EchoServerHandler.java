package co.freeside.betamax.proxy.netty;

import io.netty.buffer.*;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.util.*;
import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.*;

@ChannelHandler.Sharable
public class EchoServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        FullHttpRequest request = (FullHttpRequest) msg;
        FullHttpResponse response = new DefaultFullHttpResponse(
                HTTP_1_1,
                OK,
                request.content()
        );
        response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        FullHttpResponse response = new DefaultFullHttpResponse(
                HTTP_1_1,
                OK,
                Unpooled.copiedBuffer(cause.getClass().getSimpleName() + ": " + cause.getMessage(), CharsetUtil.UTF_8)
        );
        response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}
