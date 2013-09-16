package co.freeside.betamax.proxy.netty;

import java.io.*;
import java.util.*;
import co.freeside.betamax.message.*;
import com.google.common.base.*;
import io.netty.handler.codec.http.*;

public abstract class NettyMessageAdapter<T extends HttpMessage> extends AbstractMessage {

    protected final T delegate;
    private final ByteArrayOutputStream body = new ByteArrayOutputStream();

    protected NettyMessageAdapter(T delegate) {
        this.delegate = delegate;
    }

    public void append(HttpObject chunk) throws IOException {
        if (chunk instanceof HttpContent) {
            body.write(((HttpContent) chunk).content().array()); // TODO: this only works for some impls
        }
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
        return body.size() > 0;
    }

    @Override
    public InputStream getBodyAsBinary() throws IOException {
        // TODO: can this be done without copying the entire byte array?
        return new ByteArrayInputStream(body.toByteArray());
    }
}
