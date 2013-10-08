/*
 * Copyright 2011 the original author or authors.
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
import co.freeside.betamax.message.*;
import com.google.common.io.*;
import org.apache.http.*;

public class HttpResponseAdapter extends HttpMessageAdapter<HttpResponse> implements Response {
    public HttpResponseAdapter(HttpResponse delegate) {
        try {
            this.delegate = delegate;

            if (delegate.getEntity() != null) {
                body = ByteStreams.toByteArray(delegate.getEntity().getContent());
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected HttpResponse getDelegate() {
        return delegate;
    }

    @Override
    public int getStatus() {
        return delegate.getStatusLine().getStatusCode();
    }

    @Override
    public boolean hasBody() {
        return body != null;
    }

    @Override
    protected InputStream getBodyAsStream() {
        if (body == null) {
            // TODO: this is inconsistent with RecordedResponse - interface should make it clear what should happen in this case
            throw new IllegalStateException("cannot read the body of a response that does not have one");
        }

        return new ByteArrayInputStream(body);
    }

    @Override
    protected Reader getBodyAsReader() {
        try {
            return new InputStreamReader(getBodyAsStream(), getCharset());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private final HttpResponse delegate;
    private byte[] body;
}
