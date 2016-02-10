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

package com.gneoxsolutions.betamax.message;

import com.google.common.base.Strings;
import com.google.common.net.MediaType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.net.HttpHeaders.CONTENT_ENCODING;
import static com.google.common.net.HttpHeaders.CONTENT_TYPE;
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
        return header == null || header.length() == 0 ? DEFAULT_ENCODING : header;
    }

    @Override
    public String getHeader(String name) {
        return getHeaders().get(name);
    }

    @Override
    public final byte[] getBodyAsBinary() {
        // Credit goes to StackOverflow Post
        // http://stackoverflow.com/questions/1264709/convert-inputstream-to-byte-array-in-java
        try {
            InputStream is = getBodyAsStream();

            try {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();

                int nRead;
                byte[] data = new byte[8 * 1024]; // 8KB

                while ((nRead = is.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }

                buffer.flush();

                return buffer.toByteArray();
            } finally {
                is.close();
            }
        } catch (IOException e) {
            return new byte[0];
        }
    }

    @Override
    public final String getBodyAsText() {
        try {
            return new String(getBodyAsBinary(), getCharset());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract InputStream getBodyAsStream() throws IOException;
}
