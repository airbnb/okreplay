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
import java.util.*;
import com.google.common.io.*;

/**
 * An abstraction of an HTTP request or response. Implementations can be backed by any sort of underlying
 * implementation.
 */
public interface Message {

    /**
     * @return all HTTP headers attached to this message.
     */
    Map<String, String> getHeaders();

    /**
     * @param name an HTTP header name.
     * @return the comma-separated values for all HTTP headers with the specified name or `null` if there are no headers
     * with that name.
     */
    String getHeader(String name);

    /**
     * @param name  an HTTP header name.
     * @param value the header value that will be appended to any existing headers.
     */
    void addHeader(String name, String value);

    /**
     * @return `true` if the message currently contains a body, `false` otherwise.
     */
    boolean hasBody();

    /**
     * Returns the message body as a string.
     *
     * @return the message body as a string.
     * @throws IllegalStateException if the message does not have a body.
     */
    InputSupplier<Reader> getBodyAsText();

    /**
     * Returns the decoded message body. If the implementation stores the message body in an encoded form (e.g. gzipped) then it <em>must</em> be decoded
     * before being returned by this method
     *
     * @return the message body as binary data.
     * @throws IllegalStateException if the message does not have a body.
     */
    InputSupplier<InputStream> getBodyAsBinary();

    /**
     * @return the MIME content type of the message not including any charset.
     */
    String getContentType();

    /**
     * @return the charset of the message if it is text.
     */
    String getCharset();

    /**
     * @return the content encoding of the message, e.g. _gzip_, _deflate_ or _none_.
     */
    String getEncoding();
}