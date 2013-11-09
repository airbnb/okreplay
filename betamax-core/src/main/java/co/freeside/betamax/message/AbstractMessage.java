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
import com.google.common.base.Strings;
import com.google.common.io.InputSupplier;
import com.google.common.net.MediaType;
import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.net.HttpHeaders.*;
import static com.google.common.net.MediaType.OCTET_STREAM;

public abstract class AbstractMessage implements Message {

    public static final String DEFAULT_CONTENT_TYPE = OCTET_STREAM.toString();
    public static final String DEFAULT_CHARSET = UTF_8.toString();
    public static final String DEFAULT_ENCODING = "none";

    @Override
    public String getContentType() {
        String header = getHeader(CONTENT_TYPE);
        if (Strings.isNullOrEmpty(header)) {
            return DEFAULT_CONTENT_TYPE;
        } else {
            return MediaType.parse(header).withoutParameters().toString();
        }
    }

    @Override
    public String getCharset() {
        String header = getHeader(CONTENT_TYPE);
        if (Strings.isNullOrEmpty(header)) {
            // TODO: this isn't valid for non-text data – this method should return Optional<String>
            return DEFAULT_CHARSET;
        } else {
            return MediaType.parse(header).charset().or(UTF_8).toString();
        }
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
     * A default implementation that decodes the byte stream from
     * `getBodyAsBinary`. Implementations can override this
     * if they have a simpler way of doing it.
     */
    protected Reader getBodyAsReader() throws IOException {
        return new InputStreamReader(getBodyAsBinary().getInput(), getCharset());
    }


    private String defaultIfNullOrEmpty(String string, String defaultValue) {
        return Strings.isNullOrEmpty(string) ? defaultValue : string;
    }

}
