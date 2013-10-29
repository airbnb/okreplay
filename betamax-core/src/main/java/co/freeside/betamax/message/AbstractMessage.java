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

package co.freeside.betamax.message;

import java.io.*;
import java.util.regex.*;
import com.google.common.base.*;
import com.google.common.io.*;
import static org.apache.http.HttpHeaders.*;

public abstract class AbstractMessage implements Message {

    public static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";
    public static final String DEFAULT_CHARSET = "UTF-8";
    public static final String DEFAULT_ENCODING = "none";

    @Override
    public String getContentType() {
        String contentTypeHeader = getHeader(CONTENT_TYPE);
        if (!Strings.isNullOrEmpty(contentTypeHeader)) {
            return Splitter.on(';').splitToList(contentTypeHeader).get(0);
        } else {
            return DEFAULT_CONTENT_TYPE;
        }
    }

    @Override
    public String getCharset() {
        String declaredCharset = null;
        String header = getHeader(CONTENT_TYPE);
        if (!Strings.isNullOrEmpty(header)) {
            Matcher matcher = Pattern.compile("charset=(.*)").matcher(header);
            if (matcher.find()) {
                declaredCharset = matcher.group(1);
            }
        }
        return defaultIfNullOrEmpty(declaredCharset, DEFAULT_CHARSET);
    }

    @Override
    public String getEncoding() {
        String header = getHeader(CONTENT_ENCODING);
        return defaultIfNullOrEmpty(header, DEFAULT_ENCODING);
    }

    @Override
    public String getHeader(String name) {
        return getHeaders().get(name);
    }

    @Override
    public final InputSupplier<InputStream> getBodyAsBinary() {
        return new InputSupplier<InputStream>() {
            @Override
            public InputStream getInput() throws IOException {
                return getBodyAsStream();
            }
        };
    }

    @Override
    public final InputSupplier<Reader> getBodyAsText() {
        return new InputSupplier<Reader>() {
            @Override
            public Reader getInput() throws IOException {
                return getBodyAsReader();
            }
        };
    }

    protected abstract InputStream getBodyAsStream() throws IOException;

    /**
     * A default implementation that decodes the byte stream from `getBodyAsBinary`. Implementations can override this
     * if they have a simpler way of doing it.
     */
    protected Reader getBodyAsReader() throws IOException {
        return new InputStreamReader(getBodyAsBinary().getInput(), getCharset());
    }


    private String defaultIfNullOrEmpty(String string, String defaultValue) {
        return Strings.isNullOrEmpty(string) ? defaultValue : string;
    }

}
