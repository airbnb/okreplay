package co.freeside.betamax.proxy.netty;

import java.io.*;
import java.util.*;
import co.freeside.betamax.message.*;
import com.google.common.base.*;
import io.netty.handler.codec.http.*;

public abstract class NettyMessageAdapter<T extends FullHttpMessage> extends AbstractMessage {

    protected final T delegate;
    private byte[] body;

    protected NettyMessageAdapter(T delegate) {
        this.delegate = delegate;
    }

    @Override
    public Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<String, String>();
        for (String name : delegate.headers().names()) {
            headers.put(name, getHeader(name));
        }
        return Collections.unmodifiableMap(headers);
    }

    @Override
    public String getHeader(String name) {
        return Joiner.on(", ").join(delegate.headers().getAll(name));
    }

    @Override
    public void addHeader(String name, String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasBody() {
        return delegate.content() != null && delegate.content().isReadable();
    }

    @Override
    public InputStream getBodyAsBinary() throws IOException {
        // TODO: can this be done without copying the entire byte array?
        if (body == null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            delegate.content().getBytes(0, stream, delegate.content().readableBytes());
            body = stream.toByteArray();
        }
        return new ByteArrayInputStream(body);
    }
}
