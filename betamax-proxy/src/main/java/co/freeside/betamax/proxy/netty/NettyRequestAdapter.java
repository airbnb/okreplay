/*
 * Copyright 2013 the original author or authors.
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

package co.freeside.betamax.proxy.netty;

import java.net.*;
import co.freeside.betamax.message.*;
import io.netty.handler.codec.http.*;
import static io.netty.handler.codec.http.HttpHeaders.Names.HOST;

public class NettyRequestAdapter extends NettyMessageAdapter<HttpRequest> implements Request {

    public static NettyRequestAdapter wrap(HttpObject message) {
        if (message instanceof HttpRequest) {
            return new NettyRequestAdapter((HttpRequest) message);
        } else {
            throw new IllegalArgumentException(String.format("%s is not an instance of %s", message.getClass().getName(), HttpRequest.class.getName()));
        }
    }

    NettyRequestAdapter(HttpRequest delegate) {
        super(delegate);
    }

    @Override
    public String getMethod() {
        return delegate.getMethod().name();
    }

    @Override
    public URI getUri() {
        try {
            URI uri = new URI(delegate.getUri());
            if (uri.isAbsolute()) {
                return uri;
            } else {
                return new URI(String.format("https://%s%s", getHeader(HOST), uri));
            }
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Invalid URI in underlying request", e);
        }
    }

    public final HttpRequest getOriginalRequest() {
        return delegate;
    }

}
