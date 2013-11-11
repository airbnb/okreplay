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

package co.freeside.betamax.message.httpclient;

import java.util.*;
import co.freeside.betamax.message.*;
import co.freeside.betamax.util.MultimapUtils;
import com.google.common.base.*;
import com.google.common.collect.*;
import org.apache.http.*;

public abstract class HttpMessageAdapter<T extends HttpMessage> extends AbstractMessage implements Message {
    protected abstract T getDelegate();

    @Override
    public final Map<String, String> getHeaders() {
        ImmutableMultimap.Builder<String, String> builder = ImmutableMultimap.builder();
        for (Header header : getDelegate().getAllHeaders()) {
            builder.put(header.getName(), header.getValue());
        }
        return MultimapUtils.flatten(builder.build(), ", ");
    }

    @Override
    public final String getHeader(String name) {
        List<Header> headers = Arrays.asList(getDelegate().getHeaders(name));
        List<String> headerValues = Lists.transform(headers, new Function<Header, String>() {
            @Override
            public String apply(Header header) {
                return header.getValue();
            }
        });
        return Joiner.on(", ").skipNulls().join(headerValues);
    }

    @Override
    public final void addHeader(String name, String value) {
        getDelegate().addHeader(name, value);
    }

}
