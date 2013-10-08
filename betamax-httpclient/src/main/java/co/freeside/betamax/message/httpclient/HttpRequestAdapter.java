/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package co.freeside.betamax.message.httpclient;

import java.io.*;
import java.net.*;
import co.freeside.betamax.message.*;
import org.apache.http.*;

public class HttpRequestAdapter extends HttpMessageAdapter<HttpRequest> implements Request {

    private final HttpRequest delegate;

    public HttpRequestAdapter(HttpRequest delegate) {
        this.delegate = delegate;
    }

    @Override
    protected HttpRequest getDelegate() {
        return delegate;
    }

    @Override
    public String getMethod() {
        return delegate.getRequestLine().getMethod();
    }

    @Override
    public URI getUri() {
        return URI.create(delegate.getRequestLine().getUri());
    }

    @Override
    public boolean hasBody() {
        return delegate instanceof HttpEntityEnclosingRequest;
    }

    @Override
    protected InputStream getBodyAsStream() throws IOException {
        if (delegate instanceof HttpEntityEnclosingRequest) {
            return ((HttpEntityEnclosingRequest) delegate).getEntity().getContent();
        } else {
            throw new IllegalStateException("no body");
        }
    }
}
