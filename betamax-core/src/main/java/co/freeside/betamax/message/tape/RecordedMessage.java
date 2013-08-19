package co.freeside.betamax.message.tape;

import co.freeside.betamax.encoding.AbstractEncoder;
import co.freeside.betamax.encoding.DeflateEncoder;
import co.freeside.betamax.encoding.GzipEncoder;
import co.freeside.betamax.encoding.NoOpEncoder;
import co.freeside.betamax.message.AbstractMessage;
import co.freeside.betamax.message.Message;
import com.google.common.collect.Lists;
import com.google.common.primitives.Bytes;
import org.apache.http.HttpHeaders;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.LinkedHashMap;

public abstract class RecordedMessage extends AbstractMessage implements Message {
    public final void addHeader(final String name, String value) {
        if (headers.get(name) != null)
            headers.put(name, headers.get(name) + ", " + value);
        else
            headers.put(name, value);
    }

    public final boolean hasBody() {
        return body != null;
    }

    @Override
    public final Reader getBodyAsText() {
        String string;
        if (hasBody())
            string = body instanceof String ? (String)body : getEncoder().decode(getBodyAsBinary(), getCharset());
        else
            string = "";

        return new StringReader(string);
    }

    public final InputStream getBodyAsBinary() {
        byte[] bytes;
        if (hasBody())
            bytes = (body instanceof String ? getEncoder().encode((String) body, getCharset()) : (byte[])body);
        else
            bytes = new byte[0];

        return new ByteArrayInputStream(bytes);
    }

    private AbstractEncoder getEncoder() {
        String contentEncoding = getHeader(HttpHeaders.CONTENT_ENCODING);

        if ("gzip".equals(contentEncoding))
            return new GzipEncoder();

        if ("deflate".equals(contentEncoding))
            return new DeflateEncoder();

        return new NoOpEncoder();
    }

    public LinkedHashMap<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(LinkedHashMap<String, String> headers) {
        this.headers = headers;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }

    private LinkedHashMap<String, String> headers = new LinkedHashMap<String, String>();
    private Object body;
}
