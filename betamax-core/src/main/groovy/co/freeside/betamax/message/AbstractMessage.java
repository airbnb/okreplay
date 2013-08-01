/*
 * Copyright 2011 Rob Fletcher
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

package co.freeside.betamax.message;

import java.io.*;
import java.util.regex.*;
import java.util.zip.*;
import org.apache.commons.lang.*;
import static org.apache.http.HttpHeaders.*;

public abstract class AbstractMessage implements Message {

    public static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";
    public static final String DEFAULT_CHARSET = "UTF-8";
    public static final String DEFAULT_ENCODING = "none";

    public String getContentType() {
        String contentTypeHeader = getHeader(CONTENT_TYPE);
        if (!StringUtils.isBlank(contentTypeHeader)) {
            return StringUtils.substringBefore(contentTypeHeader, ";");
        } else {
            return DEFAULT_CONTENT_TYPE;
        }
    }

    public String getCharset() {
        String declaredCharset = null;
        String header = getHeader(CONTENT_TYPE);
        if (!StringUtils.isBlank(header)) {
            Matcher matcher = Pattern.compile("charset=(.*)").matcher(header);
            if (matcher.find()) {
                declaredCharset = matcher.group(1);
            }
        }
        return defaultIfNullOrEmpty(declaredCharset, DEFAULT_CHARSET);
    }

    public String getEncoding() {
        String header = getHeader(CONTENT_ENCODING);
        return defaultIfNullOrEmpty(header, DEFAULT_ENCODING);
    }

    public String getHeader(String name) {
        return getHeaders().get(name);
    }

    /**
     * A default implementation that decodes the byte stream from `getBodyAsBinary`. Implementations can override this
     * if they have a simpler way of doing it.
     */
    public Reader getBodyAsText() throws IOException {
        InputStream stream;
        String encoding = getEncoding();
        if ("gzip".equals(encoding)) {
            stream = new GZIPInputStream(getBodyAsBinary());
        } else if ("deflate".equals(encoding)) {
            stream = new InflaterInputStream(getBodyAsBinary());
        } else {
            stream = getBodyAsBinary();
        }
        return new InputStreamReader(stream, getCharset());
    }

    private String defaultIfNullOrEmpty(String string, String defaultValue) {
        return StringUtils.isBlank(string) ? defaultValue : string;
    }

}
