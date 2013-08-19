/*
 * Copyright 2011 Rob Fletcher
 *
 * Converted from Groovy to Java by Sean Freitag
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package co.freeside.betamax.message.filtering;

import co.freeside.betamax.message.Message;
import co.freeside.betamax.message.Response;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

public class HeaderFilteringResponse extends HeaderFilteringMessage implements Response {
    public HeaderFilteringResponse(Response response) {
        this.response = response;
    }

    protected Message getDelegate() {
        return response;
    }

    public int getStatus() {
        return response.getStatus();
    }

    public String getContentType() {
        return response.getContentType();
    }

    public void addHeader(String name, String value) {
        response.addHeader(name, value);
    }

    public boolean hasBody() {
        return response.hasBody();
    }

    public Reader getBodyAsText() {
        try {
            return response.getBodyAsText();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public InputStream getBodyAsBinary() {
        try {
            return response.getBodyAsBinary();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getCharset() {
        return response.getCharset();
    }

    public String getEncoding() {
        return response.getEncoding();
    }

    private final Response response;
}
