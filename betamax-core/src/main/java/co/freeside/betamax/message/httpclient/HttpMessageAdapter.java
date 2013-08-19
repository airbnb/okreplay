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

package co.freeside.betamax.message.httpclient;

import co.freeside.betamax.message.AbstractMessage;
import co.freeside.betamax.message.Message;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.apache.http.Header;
import org.apache.http.HttpMessage;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class HttpMessageAdapter<T extends HttpMessage> extends AbstractMessage implements Message {
    protected abstract T getDelegate();

    @Override
    public final Map<String, String> getHeaders() {
        HashMap<String, String> map = new HashMap<String, String>();
        for (Header header : getDelegate().getAllHeaders()) {
            String headerName = header.getName();
            map.put(headerName, getHeader(headerName));
        }

        return map;
    }

    @Override
    public final String getHeader(String name) {
        Header[] headerArray = getDelegate().getHeaders(name);
        List<Header> headerList = Arrays.asList(headerArray);
        List<String> headerValueList = Lists.transform(headerList, new Function<Header, String>() {
            @Override
            public String apply(Header header) {
                return header.getValue();
            }
        });
        return Joiner.on(", ").skipNulls().join(headerValueList);
    }

    @Override
    public final void addHeader(String name, String value) {
        getDelegate().addHeader(name, value);
    }

}
