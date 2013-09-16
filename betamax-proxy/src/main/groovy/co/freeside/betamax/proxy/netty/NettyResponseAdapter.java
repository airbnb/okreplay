package co.freeside.betamax.proxy.netty;

import co.freeside.betamax.message.*;
import io.netty.handler.codec.http.*;

public class NettyResponseAdapter extends NettyMessageAdapter<HttpResponse> implements Response {

    public static NettyResponseAdapter wrap(HttpObject message) {
        if (message instanceof HttpResponse) {
            return new NettyResponseAdapter((HttpResponse) message);
        } else {
            throw new IllegalArgumentException(String.format("%s is not an instance of %s", message.getClass().getName(), FullHttpResponse.class.getName()));
        }
    }

    NettyResponseAdapter(HttpResponse delegate) {
        super(delegate);
    }

    @Override
    public int getStatus() {
        return delegate.getStatus().code();
    }

}
