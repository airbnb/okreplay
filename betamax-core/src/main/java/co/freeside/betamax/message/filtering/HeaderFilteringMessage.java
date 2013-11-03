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

package co.freeside.betamax.message.filtering;

import java.io.*;
import java.util.*;
import co.freeside.betamax.message.*;
import com.google.common.io.*;
import static com.google.common.net.HttpHeaders.*;

public abstract class HeaderFilteringMessage implements Message {
    protected abstract Message getDelegate();

    public static final String PROXY_CONNECTION = "Proxy-Connection";
    public static final String KEEP_ALIVE = "Keep-Alive";
    public static final List<String> NO_PASS_HEADERS = Arrays.asList(CONTENT_LENGTH, HOST, PROXY_CONNECTION, CONNECTION, KEEP_ALIVE, PROXY_AUTHENTICATE, PROXY_AUTHORIZATION, TE, TRAILER, TRANSFER_ENCODING, UPGRADE);

    public final Map<String, String> getHeaders() {
        HashMap<String, String> headers = new HashMap<String, String>(getDelegate().getHeaders());
        for (String headerName : NO_PASS_HEADERS) {
            headers.remove(headerName);
        }

        return headers;
    }

    public final String getHeader(String name) {
        return NO_PASS_HEADERS.contains(name) ? null : getDelegate().getHeader(name);
    }

    @Override
    public void addHeader(String name, String value) {
        getDelegate().addHeader(name, value);
    }

    @Override
    public boolean hasBody() {
        return getDelegate().hasBody();
    }

    @Override
    public InputSupplier<Reader> getBodyAsText() {
        return getDelegate().getBodyAsText();
    }

    @Override
    public InputSupplier<InputStream> getBodyAsBinary() {
        return getDelegate().getBodyAsBinary();
    }

    @Override
    public String getContentType() {
        return getDelegate().getContentType();
    }

    @Override
    public String getCharset() {
        return getDelegate().getCharset();
    }

    @Override
    public String getEncoding() {
        return getDelegate().getEncoding();
    }

}
