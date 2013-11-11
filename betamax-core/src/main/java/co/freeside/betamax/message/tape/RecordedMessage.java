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

package co.freeside.betamax.message.tape;

import java.io.*;
import java.util.LinkedHashMap;
import co.freeside.betamax.encoding.*;
import co.freeside.betamax.message.*;
import com.google.common.io.ByteStreams;
import static com.google.common.net.HttpHeaders.CONTENT_ENCODING;

public abstract class RecordedMessage extends AbstractMessage implements Message {
    public final void addHeader(final String name, String value) {
        if (headers.get(name) != null) {
            headers.put(name, headers.get(name) + ", " + value);
        } else {
            headers.put(name, value);
        }
    }

    public final boolean hasBody() {
        return body != null;
    }

    @Override
    protected final Reader getBodyAsReader() throws IOException {
        String string;
        if (hasBody()) {
            string = body instanceof String ? (String) body : new String(ByteStreams.toByteArray(getBodyAsBinary()), getCharset());
        } else {
            string = "";
        }

        return new StringReader(string);
    }

    protected final InputStream getBodyAsStream() throws UnsupportedEncodingException {
        byte[] bytes;
        if (hasBody()) {
            bytes = body instanceof String ? ((String) body).getBytes(getCharset()) : (byte[]) body;
        } else {
            bytes = new byte[0];
        }

        return new ByteArrayInputStream(bytes);
    }

    private AbstractEncoder getEncoder() {
        String contentEncoding = getHeader(CONTENT_ENCODING);

        if ("gzip".equals(contentEncoding)) {
            return new GzipEncoder();
        }

        if ("deflate".equals(contentEncoding)) {
            return new DeflateEncoder();
        }

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
