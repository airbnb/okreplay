package co.freeside.betamax.message.filtering;

import co.freeside.betamax.message.Message;
import co.freeside.betamax.message.Request;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;

public class HeaderFilteringRequest extends HeaderFilteringMessage implements Request {
    public HeaderFilteringRequest(Request request) {
        this.request = request;
    }

    protected Message getDelegate() {
        return request;
    }

    public String getMethod() {
        return request.getMethod();
    }

    public URI getUri() {
        return request.getUri();
    }

    public void addHeader(String name, String value) {
        request.addHeader(name, value);
    }

    public boolean hasBody() {
        return request.hasBody();
    }

    public Reader getBodyAsText() {
        try {
            return request.getBodyAsText();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public InputStream getBodyAsBinary() {
        try {
            return request.getBodyAsBinary();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getContentType() {
        return request.getContentType();
    }

    public String getCharset() {
        return request.getCharset();
    }

    public String getEncoding() {
        return request.getEncoding();
    }

    private final Request request;
}
