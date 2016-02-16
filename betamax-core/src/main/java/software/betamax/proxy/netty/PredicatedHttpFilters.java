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

package software.betamax.proxy.netty;

import com.google.common.base.Predicate;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersAdapter;

public class PredicatedHttpFilters extends HttpFiltersAdapter {

    public static Predicate<HttpRequest> httpMethodPredicate(final HttpMethod method) {
        return new Predicate<HttpRequest>() {
            @Override
            public boolean apply(HttpRequest input) {
                return method.equals(input.getMethod());
            }
        };
    }

    private final HttpFilters delegate;
    private final Predicate<HttpRequest> predicate;

    public PredicatedHttpFilters(HttpFilters delegate, Predicate<HttpRequest> predicate, HttpRequest originalRequest) {
        super(originalRequest);
        this.delegate = delegate;
        this.predicate = predicate;
    }

    @Override
    public HttpResponse clientToProxyRequest(HttpObject httpObject) {
        if (predicate.apply(originalRequest)) {
            return delegate.clientToProxyRequest(httpObject);
        } else {
            return null;
        }
    }

    @Override
    public HttpResponse proxyToServerRequest(HttpObject httpObject) {
        if (predicate.apply(originalRequest)) {
            return delegate.proxyToServerRequest(httpObject);
        } else {
            return null;
        }
    }

    @Override
    public HttpObject serverToProxyResponse(HttpObject httpObject) {
        if (predicate.apply(originalRequest)) {
            return delegate.serverToProxyResponse(httpObject);
        } else {
            return httpObject;
        }
    }

    @Override
    public HttpObject proxyToClientResponse(HttpObject httpObject) {
        if (predicate.apply(originalRequest)) {
            return delegate.proxyToClientResponse(httpObject);
        } else {
            return httpObject;
        }
    }
}
