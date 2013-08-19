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

package co.freeside.betamax.encoding;

import com.google.common.io.CharStreams;

import java.io.*;
import java.nio.charset.Charset;

public abstract class AbstractEncoder {
    public final String decode(InputStream input, String charset) {
        try {
            return CharStreams.toString(new InputStreamReader(getDecodingInputStream(input), charset));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public final String decode(InputStream input) {
        return decode(input, Charset.defaultCharset().toString());
    }

    public final byte[] encode(String input, String charset) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            OutputStream stream = getEncodingOutputStream(out);
            stream.write(input.getBytes(charset));
            stream.flush();
            stream.close();
            return out.toByteArray();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public final byte[] encode(String input) {
        return encode(input, Charset.defaultCharset().toString());
    }

    protected abstract InputStream getDecodingInputStream(InputStream input);

    protected abstract OutputStream getEncodingOutputStream(OutputStream output);
}
