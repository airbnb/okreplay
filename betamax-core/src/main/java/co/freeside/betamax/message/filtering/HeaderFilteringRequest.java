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
