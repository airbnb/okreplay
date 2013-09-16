package co.freeside.betamax.proxy.netty;

import co.freeside.betamax.message.*;
import io.netty.handler.codec.http.*;

public class NettyResponseAdapter extends NettyMessageAdapter<FullHttpResponse> implements Response {

    public static Response wrap(HttpObject message) {
        if (message instanceof FullHttpResponse) {
            return new NettyResponseAdapter((FullHttpResponse) message);
        } else {
            throw new IllegalArgumentException(String.format("%s is not an instance of %s", message.getClass().getName(), FullHttpResponse.class.getName()));
        }
    }

    NettyResponseAdapter(FullHttpResponse delegate) {
        super(delegate);
    }

    @Override
    public int getStatus() {
        return delegate.getStatus().code();
    }

}
