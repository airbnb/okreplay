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

package com.gneoxsolutions.betamax.proxy.netty;

import com.gneoxsolutions.betamax.message.Response;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;

public class NettyResponseAdapter extends NettyMessageAdapter<HttpResponse> implements Response {

    public static NettyResponseAdapter wrap(HttpObject message) {
        if (message instanceof HttpResponse) {
            return new NettyResponseAdapter((HttpResponse) message);
        } else {
            throw new IllegalArgumentException(String.format("%s is not an instance of %s", message.getClass().getName(), FullHttpResponse.class.getName()));
        }
    }

    NettyResponseAdapter(HttpResponse delegate) {
        super(delegate);
    }

    @Override
    public int getStatus() {
        return delegate.getStatus().code();
    }

}
